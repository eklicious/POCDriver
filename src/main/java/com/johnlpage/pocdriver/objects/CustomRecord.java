package com.johnlpage.pocdriver.objects;

import com.google.gson.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.*;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * EK: This is not threadsafe but for the sake of simple load testing, we don't care
 */
public class CustomRecord {
    private static final Logger logger = LogManager.getLogger(CustomRecord.class);
    private static CustomRecord instance;
    private JsonObject metaStructure;

    public static CustomRecord getInstance(POCTestOptions o) {
        if (instance == null)
        {
            //synchronized block to remove overhead
            synchronized (CustomRecord.class)
            {
                if(instance==null)
                {
                    // if instance is null, initialize
                    instance = new CustomRecord(o);
                    logger.debug("Creating new custom record object");
                }
            }
        }
        return instance;
    }

    /**
     * Initialize this custom record
     * Take the custom doc structure and create the json object. The values will not be set and needs to be explicitly called
     */
    private CustomRecord(POCTestOptions o) {
        logger.info("Custom Template: " + o.customtemplate);
        // The custom template
        metaStructure = new JsonParser().parse(o.customtemplate).getAsJsonObject();
    }

    /**
     * For a parameter metadata array value, figure out if we are expected to generate an array of values
     * we are returning a double because we never want to return 1 as a value, rather 1.1 so we know this is an array
     * @param a json array of parameters for metadata, 3rd and 4th params are considered array min/max element values
     * @return return an array count. 1 means we don't need an array, >=1.1 means we need an array of 1 value or more
     */
    private double getArrayCount(JsonArray a) {
        int minArray;
        int maxArray;
        double x = 1;
        // assuming 3rd and 4th params are array length
        if (a.size()>=3) { // we have min array length
            minArray = a.get(2).getAsInt();
            if (a.size()>=4) {
                maxArray = a.get(3).getAsInt();
            } else {
                maxArray = minArray;
            }
            logger.debug("--- Array bounds found: [\" + minArray + \", \" + maxArray + \"]. Getting random value in between min/max.");
            x = ((new Random()).nextInt((maxArray - minArray) + 1) + minArray) + 0.1;
            logger.debug("--- Randomly determined # of array elements = " + x);
        }
        return x;
    }

    private long getRandomLong(long min, long max) {
        Random r = new Random();
        return min+((long)(r.nextDouble()*(max-min)));
    }


    /**
     * this will return a base object or an array of base objects
     * @return a base json object, e.g. string or int or an array of base objs
     */
    private Object getBaseObject(JsonObject objMeta) throws java.text.ParseException {
        Object baseObj = null;
        if (objMeta.has("str")) {
            JsonArray a = objMeta.get("str").getAsJsonArray();
            logger.debug("String min/max length: " + a.get(0).getAsInt() + "/" + a.get(1).getAsInt());
            // Generate a random string value with min/max length of above
            double arrayCnt = getArrayCount(a);
            if (arrayCnt>1) { // insert array of strings
                List<String> s2 = new ArrayList<String>();
                for (int i=0; i<(int)arrayCnt; i++) {
                    s2.add(RandomStringUtils.randomAlphabetic(a.get(0).getAsInt(), a.get(1).getAsInt()));
                }
                baseObj = s2;
            } else { // insert single string
                String s = RandomStringUtils.randomAlphabetic(a.get(0).getAsInt(), a.get(1).getAsInt());
                logger.debug("Random String value: " + s);
                baseObj = s;
            }
        } else if (objMeta.has("int")) {
            logger.debug("objMeta: " + objMeta.toString());
            JsonArray a = objMeta.get("int").getAsJsonArray();
            logger.debug("int min/max value: " + a.get(0).getAsInt() + "/" + a.get(1).getAsInt());
            // Generate a random int value with min/max value of above
            double arrayCnt = getArrayCount(a);
            if (arrayCnt > 1) { // insert array of ints
                List<Integer> s2 = new ArrayList<Integer>();
                for (int i = 0; i < (int) arrayCnt; i++) {
                    int x = (new Random()).nextInt((a.get(1).getAsInt() - a.get(0).getAsInt()) + 1) + a.get(0).getAsInt();
                    s2.add(x);
                    logger.debug("Getting array of ints. Base int: " + x);
//                    s2.add(ThreadLocalRandom.current().nextInt(a.get(0).getAsInt(), a.get(1).getAsInt() + 1));
                }
                baseObj = s2;
            } else { // insert single int
                int i = (new Random()).nextInt((a.get(1).getAsInt() - a.get(0).getAsInt()) + 1) + a.get(0).getAsInt();
                logger.debug("base int: " + i);
                baseObj = i;
            }
        } else if (objMeta.has("dt")) {
            JsonArray a = objMeta.get("dt").getAsJsonArray();
            logger.debug("dt min/max value: " + a.get(0).toString() + "/" + a.get(1).toString());

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date minDt = formatter.parse(a.get(0).getAsString());
            Date maxDt = formatter.parse(a.get(1).getAsString());

            // Generate a random date value with min/max value of above
            double arrayCnt = getArrayCount(a);
            if (arrayCnt > 1) { // insert array of dts
                List<Date> s2 = new ArrayList<Date>();
                for (int i = 0; i < (int) arrayCnt; i++) {
                    long random = getRandomLong(minDt.getTime(), maxDt.getTime());
//                    long random = ThreadLocalRandom.current().nextLong(minDt.getTime(), maxDt.getTime());
                    Date date = new Date(random);
                    s2.add(date);
                }
                baseObj = s2;
            } else { // insert single dt
                long random = getRandomLong(minDt.getTime(), maxDt.getTime());
//                long random = ThreadLocalRandom.current().nextLong(minDt.getTime(), maxDt.getTime());
                baseObj = new Date(random);
                logger.debug("base date: " + baseObj);
            }
        }
        return baseObj;
    }

    /**
     * Get a sample doc based on metaStructure object
     * @return return a bson document
     */
    public Document getDoc(int workerId, int sequence) throws java.text.ParseException {
        Document d = new Document();

        // add _id
        logger.debug("Custom doc worker id: " + workerId + " , sequence #: " + sequence);
        Document oid = new Document("w",workerId).append("i", sequence);
        d.append("_id", oid);

        for (Map.Entry<String, JsonElement> valueEntry : metaStructure.entrySet()) {
            logger.debug("Field Name: " + valueEntry.getKey());

            JsonObject objMeta = valueEntry.getValue().getAsJsonObject();
            logger.debug("Field metadata: " + objMeta.toString());

            if (objMeta.has("str") || objMeta.has("int") || objMeta.has("dt")) {
                d.append(valueEntry.getKey(), getBaseObject(objMeta));
            } else if (objMeta.has("doc")) { // we are only going 1 nested doc in...
                JsonObject nestedMetaStructure = objMeta.get("doc").getAsJsonObject();
                logger.debug("Nested Object found: " + nestedMetaStructure.toString());
                Document nestedDoc = new Document();
                for (Map.Entry<String, JsonElement> nestedValueEntry : nestedMetaStructure.entrySet()) {
                    JsonObject nestedObjectMeta = nestedValueEntry.getValue().getAsJsonObject();
                    logger.debug("- Nested Field Name: " + nestedValueEntry.getKey());
                    if (nestedObjectMeta.has("str")
                            || nestedObjectMeta.has("int")
                            || nestedObjectMeta.has("dt") ) {
                        logger.debug("- Getting nested based object...");
                        nestedDoc.append(nestedValueEntry.getKey(), getBaseObject(nestedObjectMeta));
                    }
                }
                d.append(valueEntry.getKey(), nestedDoc);
            }
        }
        logger.debug("Returning Sample Doc: " + d.toJson());
        return d;
    }

    public static void main(String[] args) throws ParseException, java.text.ParseException {
        logger.debug("Testing metastructure parsing");
        StringBuilder sampleMetastructureJson = new StringBuilder();
        sampleMetastructureJson.append("{");
        sampleMetastructureJson.append("firstname: {str:[7,10]}, ");
        sampleMetastructureJson.append("nicknames: {str:[3,10,1,2]}, ");
        sampleMetastructureJson.append("age: {int:[0,100]}, ");
        sampleMetastructureJson.append("favoritenumbers: {int:[0,100,3]}, ");
        sampleMetastructureJson.append("birthdate: {dt:[\"1940-01-01 14:05:09\",\"2000-11-14 14:05:09\"]}, ");
        sampleMetastructureJson.append("phones: {doc:{type:{str:[7,10]},number:{int:[2011234567,2019759876]},randomarray:{int:[2,9,3,4]}}}"); // phones: [{type:"home",number:"123456"}]
        sampleMetastructureJson.append("}");
        logger.debug("sampleMetastructureJson: " + sampleMetastructureJson.toString());

        StringBuilder metastructure = new StringBuilder();
        metastructure.append("{");
        metastructure.append("documentAudit: {doc:{expirationDatetime:{dt:[\"2020-01-01 14:05:09\",\"2024-11-14 14:05:09\"]},createdDate:{dt:[\"2018-01-01 14:05:09\",\"2018-04-14 14:05:09\"]}}},");
        metastructure.append("processInfo: {doc:{statusDate:{dt:[\"2018-01-01 14:05:09\",\"2018-04-14 14:05:09\"]},status:{str:[4,8]},state:{str:[4,8]},updatedByApp:{str:[4,8]}}},");
        metastructure.append("recordSource: {doc:{fileArchives_id:{int:[1000000,10000000]},createDate:{dt:[\"2018-01-01 14:05:09\",\"2018-04-14 14:05:09\"]}}},");
        metastructure.append("recordInfo: {doc:{recordSizeBytes:{int:[500,2000]},recordHash:{str:[16,50]},recordId:{int:[1000000,10000000]},recordSequence:{int:[1000000,10000000]}}},");
        metastructure.append("client: {doc:{clientId:{str:[5,20]},clientName:{str:[10,50]}}},");
        metastructure.append("participant: {doc:{participantId:{str:[5,20]}}},");
        metastructure.append("content: {str:[100,2000]}");
        metastructure.append("}");
        logger.debug("metastructure: " + metastructure.toString());

        String[] pocArgs = new String[2];
        pocArgs[0] = "-customtemplate";
        pocArgs[1] = metastructure.toString();
        POCTestOptions o = new POCTestOptions(pocArgs);

        CustomRecord cr = CustomRecord.getInstance(o);

        logger.debug("Sample Doc: " + cr.getDoc(1, 1).toJson());


        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:30000"));
        MongoDatabase db = mongoClient.getDatabase("POCDB");
        MongoDatabase admindb = mongoClient.getDatabase("admin");
        Document d = admindb.runCommand(new Document("serverStatus", 1));
        logger.debug(d.toJson());

        MongoIterable<String> collection = db.listCollectionNames();
        MongoCursor<String> cursor = collection.iterator();
        while(cursor.hasNext()){
            String table = cursor.next();
            logger.debug(table);
        }


        MongoCollection<Document> coll = db.getCollection("ek");

        String pipedAggQuery = "{$match: { \"recordInfo.recordSizeBytes\": {$lt: 700}}}|{$project: {_id:0, recordSizeBytes:\"$recordInfo.recordSizeBytes\", expirationYear: {$year:\"$documentAudit.expirationDatetime\"}}}|{$group: {_id: \"$expirationYear\", totalBytes: {$sum: 1}}}";
//        JsonObject aggJson = new JsonParser().parse(aggQuery).getAsJsonObject();
//        System.out.println(aggJson);
        List<Document> l = new ArrayList<Document>();
        logger.debug("pipedAggQuery: " + pipedAggQuery);
        String[] pipedAggArray = pipedAggQuery.split(Pattern.quote("|"));
        for (String s: pipedAggArray) {
            logger.debug(s);
            l.add(Document.parse(s));
        }

        AggregateIterable<Document> output = coll.aggregate(l);
        for (Document dbObject : output)
        {
            logger.debug(dbObject);
        }


    }
}
