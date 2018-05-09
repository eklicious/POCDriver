package com.johnlpage.pocdriver;

import com.johnlpage.pocdriver.objects.CustomRecord;
import com.johnlpage.pocdriver.objects.POCTestOptions;
import com.johnlpage.pocdriver.objects.POCTestResults;
import com.johnlpage.pocdriver.objects.TestRecord;
import com.mongodb.MongoClient;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.descending;

/**
 * EK: Attempting to make this code more customizable rather than random data
 * TODO: Update wiki about how to use custom template and custom agg
 * TODO: Add ability to generate schema and indexes prior to actually loading data perhaps
 */
public class MongoWorker implements Runnable {

    private static final Logger logger = LogManager.getLogger(MongoWorker.class);

    private MongoClient mongoClient;
    private MongoDatabase db; // use this for db.command in agg query
    private MongoCollection<Document> coll;
    private ArrayList<MongoCollection<Document>> colls;
    private POCTestOptions testOpts;
    private POCTestResults testResults;
    private int workerID;
    private int sequence;
    private int numShards = 0;
    private Random rng;
    private ZipfDistribution zipf;
    private boolean workflowed = false;
    private boolean zipfian = false;
    private String workflow;
    private int workflowStep = 0;
    private ArrayList<Document> keyStack;
    private int lastCollection;
    private int maxCollections;

    private void ReviewShards() {
        if (testOpts.sharded && !testOpts.singleserver) {
            // I'd like to pick a shard and write there - it's going to be
            // faster and
            // We can ensure we distribute our workers over out shards
            // So we will tell mongo that's where we want our records to go
            MongoDatabase admindb = mongoClient.getDatabase("admin");
            Boolean split = false;

            while (!split) {

                try {
                    admindb.runCommand(new Document("split",
                            testOpts.databaseName + "." + testOpts.collectionName)
                            .append("middle",
                                    new Document("_id", new Document("w",
                                            workerID).append("i", sequence + 1))));
                    split = true;
                } catch (Exception e) {

                    if (e.getMessage().contains("is a boundary key of existing")) {
                        split = true;
                    } else {
                        if (!e.getMessage().contains("could not aquire collection lock"))
                            logger.error(e.getMessage());
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ignored) {
                        }
                    }
                }

            }
            // And move that to a shard - which shard? take my workerid and mod
            // it with the number of shards
            int shardno = workerID % testOpts.numShards;
            // Get the name of the shard

            MongoCursor<Document> shardlist = mongoClient.getDatabase("config")
                    .getCollection("shards").find().skip(shardno).limit(1).iterator();
            String shardName = "";
            while (shardlist.hasNext()) {
                Document obj = shardlist.next();

                shardName = obj.getString("_id");
            }

            boolean move = false;
            while (!move) {
                try {
                    admindb.runCommand(new Document("moveChunk",
                            testOpts.databaseName + "." + testOpts.collectionName)
                            .append("find",
                                    new Document("_id", new Document("w",
                                            workerID).append("i", sequence + 1)))
                            .append("to", shardName));
                    move = true;
                } catch (Exception e) {
                    logger.error(e.getMessage());

                    if (e.getMessage().contains("that chunk is already on that shard")) {
                        move = true;
                    } else {
                        if (!e.getMessage().contains("could not aquire collection lock"))
                            logger.error(e.getMessage());
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ignored) {
                        }
                    }
                }


            }

            numShards = testOpts.numShards;
        }
    }

    MongoWorker(MongoClient c, POCTestOptions t, POCTestResults r, int id) {
        mongoClient = c;

        //Ping
        c.getDatabase("admin").runCommand(new Document("ping", 1));
        testOpts = t;
        testResults = r;
        workerID = id;
        db = mongoClient.getDatabase(testOpts.databaseName);
        maxCollections = testOpts.numcollections;
        String baseCollectionName = testOpts.collectionName;
        if (maxCollections > 1) {
            colls = new ArrayList<MongoCollection<Document>>();
            lastCollection = 0;
            for (int i = 0; i < maxCollections; i++) {
                String str = baseCollectionName + i;
                colls.add(db.getCollection(str));
            }
        } else {
            coll = db.getCollection(baseCollectionName);
        }

        // id
        sequence = getHighestID();

        ReviewShards();
        rng = new Random();
        if (testOpts.zipfian) {
            zipfian = true;
            zipf = new ZipfDistribution(testOpts.zipfsize, 0.99);
        }

        if (testOpts.workflow != null) {
            workflow = testOpts.workflow;
            workflowed = true;
            keyStack = new ArrayList<Document>();
        }

    }

    private int getNextVal(int mult) {
        int rval;
        if (zipfian) {
            rval = zipf.sample();
        } else {
            rval = (int) Math.abs(Math.floor(rng.nextDouble() * mult));
        }
        return rval;
    }

    private int getHighestID() {
        int highestId = 0;

        rotateCollection();
        Document query = new Document();

        //TODO Refactor the query for 3.0 driver
        Document limits = new Document("$gt", new Document("w", workerID));
        limits.append("$lt", new Document("w", workerID + 1));

        query.append("_id", limits);

        logger.debug("Query highest id: " + query.toJson());
        Document myDoc = coll.find(query).projection(include("_id"))
                .sort(descending("_id"))
                .first();
        if (myDoc != null) {
            Document id = (Document) myDoc.get("_id");
            highestId = id.getInteger("i") + 1;
            logger.debug("my highest id doc found: " + highestId);
        } else {
            logger.debug("my highest id doc NOT found");
        }
        return highestId;
    }

    //This one was a major rewrite as the whole Bulk Ops API changed in 3.0

    private void flushBulkOps(List<WriteModel<Document>> bulkWriter) throws Exception {
        logger.debug("flushBulkOps: " + bulkWriter);
        // Time this.
        rotateCollection();
        Date startTime = new Date();

        //This is where ALL writes are happening
        //So this can fail part way through if we have a failover
        //In which case we resubmit it

        boolean submitted = false;
        BulkWriteResult bwResult = null;

        logger.debug("bulkWriter.isEmpty(): " + bulkWriter.isEmpty());
        while (!submitted && !bulkWriter.isEmpty()) {  // can be empty if we removed a Dupe key error
            try {
                submitted = true;
                bwResult = coll.bulkWrite(bulkWriter);
                logger.debug("bwResult=" + bwResult);
            } catch (Exception e) {
                logger.error("bulk operation exception: " + e.getMessage());
                //We had a problem with this bulk op - some may be completed, some may not

                //I need to resubmit it here
                String error = e.getMessage();

                //Check if it's a sup key and remove it
                Pattern p = Pattern.compile("dup key: \\{ : \\{ w: (.*?), i: (.*?) }");
                //	Pattern p = Pattern.compile("dup key");

                Matcher m = p.matcher(error);
                if (m.find()) {
                    //int thread = Integer.parseInt(m.group(1));
                    int uniqid = Integer.parseInt(m.group(2));
                    boolean found = false;
                    for (Iterator<? super WriteModel<Document>> iter = bulkWriter.listIterator(); iter.hasNext(); ) {
                        //Check if it's a InsertOneModel

                        Object o = iter.next();
                        if (o instanceof InsertOneModel<?>) {
                            @SuppressWarnings("unchecked")
                            InsertOneModel<Document> a = (InsertOneModel<Document>) o;
                            Document id = (Document) a.getDocument().get("_id");

                            //int opthread=id.getInteger("w");
                            //int opid = id.getInteger("i");
                            if (id.getInteger("i") == uniqid) {
                                iter.remove();
                                found = true;
                            }
                        }
                    }
                    if (!found) {
                        logger.error("Cannot find failed op in batch!");
                    }
                } else {
                    // Some other error occurred - possibly MongoCommandException, MongoTimeoutException
                    logger.error(e.getClass().getSimpleName() + ": " + error);
                    // Print a full stacktrace since we're in debug mode
                    if (testOpts.debug)
                        e.printStackTrace();
                }
                submitted = false;
            }
        }

        Long taken = (new Date()).getTime() - startTime.getTime();

        if (bwResult!=null) {
            int insertCnt = bwResult.getInsertedCount();
            int updateCnt = bwResult.getMatchedCount();

            // If the bulk op is slow - ALL those ops were slow

            if (taken > testOpts.slowThreshold) {
                testResults.RecordSlowOp("inserts", insertCnt);
                testResults.RecordSlowOp("updates", updateCnt);
            }
            testResults.RecordOpsDone("inserts", insertCnt);
        } else {
            throw new Exception("Bulk writer results is null.");
        }

    }

    private Document findQuery() {
        logger.debug("############################ Find Query #################################");
        // Find Query using _id
        rotateCollection();
        Document query = new Document();
        int range = sequence * testOpts.workingset / 100;
        int rest = sequence - range;

        int recordNum = rest + getNextVal(range);

        query.append("_id", new Document("w", workerID).append("i", recordNum));
        Date startTime = new Date();
        Document myDoc;
        List<String> projFields = new ArrayList<String>(testOpts.numFields);

        // TODO: EK I'm not testing project fields when custom template is set the first part of if will always run in this case
        if (testOpts.projectFields == 0 || testOpts.customtemplate!=null) {
            myDoc = coll.find(query).first();
        } else {
            int numProjFields = (testOpts.projectFields <= testOpts.numFields) ? testOpts.projectFields : testOpts.numFields;
            int i = 0;
            while (i < numProjFields) {
                projFields.add("fld" + i);
                i++;
            }
            myDoc = coll.find(query).projection(fields(include(projFields))).first();
        }

        if (myDoc != null) {
            Date endTime = new Date();
            Long taken = endTime.getTime() - startTime.getTime();
            if (taken > testOpts.slowThreshold) {
                testResults.RecordSlowOp("keyqueries", 1);
            }
            testResults.RecordOpsDone("keyqueries", 1);
        }
        return myDoc;
    }

    private void aggQuery() {
        logger.debug("############################ Aggregation Query #################################");
        // Run aggregation query
        rotateCollection();

        Date startTime = new Date();

        String pipedAggQuery = testOpts.customagg; // example: "{$match: { \"recordInfo.recordSizeBytes\": {$lt: 700}}}|{$project: {_id:0, recordSizeBytes:\"$recordInfo.recordSizeBytes\", expirationYear: {$year:\"$documentAudit.expirationDatetime\"}}}|{$group: {_id: \"$expirationYear\", totalBytes: {$sum: 1}}}";
        List<Document> l = new ArrayList<Document>();
        logger.debug("pipedAggQuery: " + pipedAggQuery);
        String[] pipedAggArray = pipedAggQuery.split(Pattern.quote("|"));
        for (String s: pipedAggArray) {
            logger.debug("1 pipe: " + s);
            l.add(Document.parse(s));
        }

        AggregateIterable<Document> output = coll.aggregate(l);
//        for (Document dbObject : output)
//        {
//            logger.debug(dbObject);
//        }


        Date endTime = new Date();
        Long taken = endTime.getTime() - startTime.getTime();
        if (taken > testOpts.slowThreshold) {
            testResults.RecordSlowOp("aggqueries", 1);
        }
        testResults.RecordOpsDone("aggqueries", 1);

    }

    private void rangeQuery() {
        logger.debug("############################ Range Query #################################");
        // Range Query based on id, e.g. 10 docs greater than _id
        rotateCollection();
        Document query = new Document();
        List<String> projFields = new ArrayList<String>(testOpts.numFields);
        int recordNo = getNextVal(sequence);
        query.append("_id", new Document("$gt", new Document("w", workerID).append("i", recordNo)));
        Date startTime = new Date();
        MongoCursor<Document> cursor;
        // TODO EK: if custom template don't ever use projections
        if (testOpts.projectFields == 0 || testOpts.customtemplate!=null) {
            cursor = coll.find(query).limit(testOpts.rangeDocs).iterator();
        } else {
            int numProjFields = (testOpts.projectFields <= testOpts.numFields) ? testOpts.projectFields : testOpts.numFields;
            int i = 0;
            while (i < numProjFields) {
                projFields.add("fld" + i);
                i++;
            }
            cursor = coll.find(query).projection(fields(include(projFields))).limit(testOpts.rangeDocs).iterator();
        }
        while (cursor.hasNext()) {

            @SuppressWarnings("unused")
            Document obj = cursor.next();
        }
        cursor.close();

        Date endTime = new Date();
        Long taken = endTime.getTime() - startTime.getTime();
        if (taken > testOpts.slowThreshold) {
            testResults.RecordSlowOp("rangequeries", 1);
        }
        testResults.RecordOpsDone("rangequeries", 1);

    }

    private void rotateCollection() {
        if (maxCollections > 1) {
            coll = colls.get(lastCollection);
            lastCollection = (lastCollection + 1) % maxCollections;
        }
    }

    private void updateSingleRecord(List<WriteModel<Document>> bulkWriter) throws ParseException {
        updateSingleRecord(bulkWriter, null);
    }

    private void updateSingleRecord(List<WriteModel<Document>> bulkWriter, Document key) throws ParseException {
        logger.debug("############################ Update Single Query #################################");
        // Key Query
        rotateCollection();
        Document query = new Document();
        Document change;

        int recordNo = 0;
        if (key == null) {
            int range = sequence * testOpts.workingset / 100;
            int rest = sequence - range;

            recordNo = rest + getNextVal(range);

            query.append("_id", new Document("w", workerID).append("i", recordNo));
        } else {
            query.append("_id", key);
        }

        int updateFields = (testOpts.updateFields <= testOpts.numFields) ? testOpts.updateFields : testOpts.numFields;

        if (testOpts.customtemplate!=null) { // replace the whole existing doc with a new doc
            change = CustomRecord.getInstance(testOpts).getDoc(workerID, recordNo);
            logger.debug("Replacing custom template document");
            bulkWriter.add(new ReplaceOneModel<Document>(query, change, (new UpdateOptions()).upsert(true)));
        } else { // do it the old way
            if (updateFields == 1) {
                long changedField = (long) getNextVal((int) testOpts.NUMBER_SIZE);
                Document fields = new Document("fld0", changedField);
                change = new Document("$set", fields);
                logger.debug("Update doc... updateFields==1. change doc=" + change.toJson() + ", filter: " + query.toJson());
            } else {
                TestRecord tr = createNewRecord();
                tr.internalDoc.remove("_id");
                change = new Document("$set", tr.internalDoc);
            }

            if (!testOpts.findandmodify) {
                logger.debug("Update many...");
                bulkWriter.add(new UpdateManyModel<Document>(query, change));
            } else {
                logger.debug("findOneAndUpdate...");
                this.coll.findOneAndUpdate(query, change); //These are immediate not batches
            }
        }

        testResults.RecordOpsDone("updates", 1);
    }

    private TestRecord createNewRecord() {
        int[] arr = new int[2];
        arr[0] = testOpts.arraytop;
        arr[1] = testOpts.arraynext;
        return new TestRecord(testOpts.numFields, testOpts.depth, testOpts.textFieldLen,
                workerID, sequence, testOpts.NUMBER_SIZE,
                arr, testOpts.blobSize);
    }

    /**
     * ek: updating insert new record to allow for custom templates rather than an arbitrary docs
     * @param bulkWriter stores list of all operations
     * @return the document that got inserted
     */
    private Document insertNewRecord(List<WriteModel<Document>> bulkWriter) throws ParseException {
        logger.debug("############################ Insert new record #################################");
        Document d;
        sequence++;
        if (testOpts.customtemplate!=null) {
            CustomRecord cr = CustomRecord.getInstance(testOpts);
            logger.debug("Sequence: " + sequence);
            d = cr.getDoc(workerID, sequence);
        } else { // standard arbitrary document
            // This generates a new java document object each time
            d = createNewRecord().internalDoc;
        }
        bulkWriter.add(new InsertOneModel<Document>(d));
        return d;
    }


    public void run() {
        // Use a bulk inserter - even if ony for one
        List<WriteModel<Document>> bulkWriter;

        try {
            bulkWriter = new ArrayList<WriteModel<Document>>();
            int bulkOps = 0;

            int c = 0;
            logger.debug("Worker thread " + workerID + " Started.");
            while (testResults.GetSecondsElapsed() < testOpts.duration) {
                c++;
                //Timer isn't granular enough to sleep for each
                if (testOpts.opsPerSecond > 0) {
                    double threads = testOpts.numThreads;
                    double opsPerThreadSecond = testOpts.opsPerSecond / threads;
                    double sleepTimeMs = 1000 / opsPerThreadSecond;

                    if (c == 1) {
                        //First time randomise

                        Random r = new Random();
                        sleepTimeMs = r.nextInt((int) Math.floor(sleepTimeMs));

                    }
                    Thread.sleep((int) Math.floor(sleepTimeMs));
                }
                if (!workflowed) {
                    // Choose the type of op
                    int allOps = testOpts.insertops + testOpts.keyqueries
                            + testOpts.updates + testOpts.rangequeries
                            + testOpts.arrayupdates;
                    int randOp = getNextVal(allOps);

                    if (randOp < testOpts.insertops) {
                        logger.debug("Random Op: inserting new record");
                        insertNewRecord(bulkWriter);
                        bulkOps++;
                    } else if (randOp < testOpts.insertops + testOpts.keyqueries) {
                        logger.debug("Random Op: find query");
                        findQuery();
                    } else if (randOp < testOpts.insertops + testOpts.keyqueries + testOpts.rangequeries) {
                        logger.debug("Random Op: range query");
                        rangeQuery();
                    } else {
                        // An in place single field update
                        // fld 0 - set to random number
                        logger.debug("Random Op: update single record");
                        updateSingleRecord(bulkWriter);
                        if (!testOpts.findandmodify)
                            bulkOps++;
                    }
                } else {
                    String workflowOps = workflow.substring(workflowStep, workflowStep + 1);

                    logger.debug("Following a preset workflow: [" + workflow + "] " + workflowOps);
                    if (workflowOps.equals("i")) {
                        // Insert a new record, push it's key onto our stack
                        Document d = insertNewRecord(bulkWriter);
                        logger.debug("Adding to keystack " + d.toJson());
                        keyStack.add((Document) d.get("_id"));
                        bulkOps++;
                    } else if (workflowOps.equals("u")) {
                        if (keyStack.size() > 0) {
                            updateSingleRecord(bulkWriter, keyStack.get(keyStack.size() - 1));
                            if (!testOpts.findandmodify)
                                bulkOps++;
                        }
                    } else if (workflowOps.equals("p")) {
                        // Pop the top thing off the stack
                        if (keyStack.size() > 0) {
                            keyStack.remove(keyStack.size() - 1);
                        }
                    } else if (workflowOps.equals("k")) {
                        // Find a new record an put it on the stack
                        Document r = findQuery();
                        if (r != null) {
                            keyStack.add((Document) r.get("_id"));
                        }
                    } else if (workflowOps.equals("a")) { // run custom agg query that essentially does a full table scan aggregation, ie don't expect any filtering like _id
                        aggQuery();
                    }

                    // If we have reached the end of the wfops then reset
                    workflowStep++;
                    if (workflowStep >= workflow.length()) {
                        workflowStep = 0;
                        keyStack = new ArrayList<Document>();
                    }
                }

                if (c % testOpts.batchSize == 0) {
                    logger.debug("c=" + c + ", batchSize=" + testOpts.batchSize + ", bulkops=" + bulkOps + ", bulkWriter=" + bulkWriter);
                    if (bulkOps > 0) {
                        flushBulkOps(bulkWriter);
                        bulkWriter.clear();
                        bulkOps = 0;
                        // Check and see if we need to rejig sharding
                        if (numShards != testOpts.numShards) {
                            ReviewShards();
                        }
                    }
                }

            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            if (testOpts.debug)
                e.printStackTrace();
        }
    }
}
