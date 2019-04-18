package com.johnlpage.pocdriver;


import com.johnlpage.pocdriver.objects.POCTestOptions;
import com.johnlpage.pocdriver.objects.POCTestResults;
import com.johnlpage.pocdriver.objects.TestRecord;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

class LoadRunner {

    private static final Logger logger = LogManager.getLogger(LoadRunner.class);

    private MongoClient mongoClient;


    private void PrepareSystem(POCTestOptions testOpts, POCTestResults results) {
        MongoDatabase db;
        MongoCollection<Document> coll;
        //Create indexes and suchlike
        db = mongoClient.getDatabase(testOpts.databaseName);
        coll = db.getCollection(testOpts.collectionName);
        if (testOpts.emptyFirst) {
            coll.drop();
        }

        TestRecord testRecord = new TestRecord(testOpts);
        List<String> fields = testRecord.listFields();
        for (int x = 0; x < testOpts.secondaryidx; x++) {
            coll.createIndex(new Document(fields.get(x), 1));
        }
        if (testOpts.fulltext) {
            IndexOptions options = new IndexOptions();
            options.background(true);
            BasicDBObject weights = new BasicDBObject();
            weights.put("lorem", 15);
            weights.put("_fulltext.text", 5);
            options.weights(weights);
            Document index = new Document();
            index.put("$**", "text");
            coll.createIndex(index, options);
        }

        results.setInitialCount(coll.count());
        //Now have a look and see if we are sharded
        //And how many shards and make sure that the collection is sharded
        if (!testOpts.singleserver) {
            ConfigureSharding(testOpts);
        }


    }

    private void ConfigureSharding(POCTestOptions testOpts) {
        // EK: Disable sharding reconfiguration...
//        MongoDatabase admindb = mongoClient.getDatabase("admin");
//        Document cr = admindb.runCommand(new Document("serverStatus", 1));
//        if (cr.getDouble("ok") == 0) {
//            logger.debug(cr.toJson());
//            return;
//        }
//
//        String procname = (String) cr.get("process");
//        if (procname != null && procname.contains("mongos")) {
//            testOpts.sharded = true;
//            //Turn the auto balancer off - good code rarely needs it running constantly
//            MongoDatabase configdb = mongoClient.getDatabase("config");
//            MongoCollection<Document> settings = configdb.getCollection("settings");
//            settings.updateOne(eq("_id", "balancer"), new Document("$set", new Document("stopped", true)));
//            try {
//                admindb.runCommand(new Document("enableSharding", testOpts.databaseName));
//            } catch (Exception e) {
//                if (!e.getMessage().contains("already enabled"))
//                    logger.error(e.getMessage());
//            }
//
//
//            try {
//                admindb.runCommand(new Document("shardCollection",
//                        testOpts.databaseName + "." + testOpts.collectionName).append("key", new Document("_id", 1)));
//            } catch (Exception e) {
//                if (!e.getMessage().contains("already"))
//                    logger.error(e.getMessage());
//            }
//
//
//            //See how many shards we have in the system - and get a list of their names
//            MongoCollection<Document> shards = configdb.getCollection("shards");
//            MongoCursor<Document> shardc = shards.find().iterator();
//            testOpts.numShards = 0;
//            while (shardc.hasNext()) {
//                shardc.next();
//                testOpts.numShards++;
//
//            }
//
//        }
    }

    void RunLoad(POCTestOptions testOpts, POCTestResults testResults) {

        PrepareSystem(testOpts, testResults);
        // Report on progress by looking at testResults
        POCTestReporter reporter = new POCTestReporter(testResults, mongoClient, testOpts);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(reporter, 0, testOpts.reportTime, TimeUnit.SECONDS);


        // Using a thread pool we keep filled
        ExecutorService testexec = Executors
                .newFixedThreadPool(testOpts.numThreads);

        // Allow for multiple clients to run -
        // Check for testOpts.threadIdStart - this should be an integer to start
        // the 'workerID' for each set of threads.
        int threadIdStart = testOpts.threadIdStart;
        for (int i = threadIdStart; i < (testOpts.numThreads + threadIdStart); i++) {
            testexec.execute(new MongoWorker(mongoClient, testOpts, testResults, i));
        }

        testexec.shutdown();

        try {
            testexec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            executor.shutdown();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }

        // do final report
        reporter.finalReport();
    }

    LoadRunner(POCTestOptions testOpts) {
        try {
            //For not authentication via connection string passing of user/pass only
            mongoClient = new MongoClient(new MongoClientURI(testOpts.connectionDetails));
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
