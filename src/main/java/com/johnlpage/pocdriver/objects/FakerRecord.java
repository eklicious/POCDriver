package com.johnlpage.pocdriver.objects;

import com.github.javafaker.Faker;
import com.google.gson.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.*;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * EK: This is not threadsafe but for the sake of simple load testing, we don't care
 * Samples can be seen here: https://java-faker.herokuapp.com/
 */
public class FakerRecord {
    private static final Logger logger = LogManager.getLogger(FakerRecord.class);
    private static FakerRecord instance;
    private static Faker faker = new Faker();
    private JsonObject metaStructure;

    public static FakerRecord getInstance(POCTestOptions o) {
        if (instance == null)
        {
            //synchronized block to remove overhead
            synchronized (FakerRecord.class)
            {
                if(instance==null)
                {
                    // if instance is null, initialize
                    instance = new FakerRecord(o);
                    logger.debug("Creating new faker record object");
                }
            }
        }
        return instance;
    }

    /**
     * Initialize this faker record
     * Take the faker doc structure and create the json object. The values will not be set and needs to be explicitly called
     */
    private FakerRecord(POCTestOptions o) {
        logger.info("Faker Template: " + o.fakertemplate);
        // The custom template
        metaStructure = new JsonParser().parse(o.fakertemplate).getAsJsonObject();
    }

    /**
     * Get an arbitrary array count
     */
    private double getArrayCount(int cnt) {
        double x = 1;
        // assuming 3rd and 4th params are array length
        if (cnt<=1) { // just return 1
            // do nothing. just return 1
        } else {
            x = ((new Random()).nextInt(cnt)) + 1.1;
            logger.debug("--- Randomly determined # of array elements = " + x);
        }
        return x;
    }

//    private long getRandomLong(long min, long max) {
//        Random r = new Random();
//        return min+((long)(r.nextDouble()*(max-min)));
//    }


    /**
     * this will return a base object or an array of base objects
     * We are going to use reflection to get some base object back from faker
     * @return a base json object, e.g. string or int or an array of base objs
     */
    private Object getBaseObject(JsonObject objMeta) throws Exception {
        Object returnObj = null;
        Object baseObj = faker;
        Object tempObj = null; // store a temp object for the last method invocation so we don't overwrite the base object
        Method method = null;
        String[] methodArr = null;
        int arrayCnt = 0;
        Class<?>[] paramTypes = null;
        Object[] paramArgs = null;

        // Find the key name so we can use reflection on the methods. There should only be 1 mapped entry...
        for (Map.Entry<String, JsonElement> valueEntry : objMeta.entrySet()) {
            logger.debug("Method Names: " + valueEntry.getKey());
            methodArr = valueEntry.getKey().split("\\.");
            arrayCnt = valueEntry.getValue().getAsInt();
        }
        double randomArrayCnt = getArrayCount(arrayCnt);
        logger.debug("random array item count: " + randomArrayCnt);


        // iterate through method names till we get to the last method in the array
        int cnt = 1;
        for (String methodName: methodArr) {
            logger.debug("Method Name: " + methodName);
            // Check to see if there are parameters for this method
            if (methodName.contains("(")) { // we have params...
                // We are implicitly finding out the data types of params. Either it's an int or date. anything else is out of scope
                String paramsList = methodName.substring(methodName.indexOf("(")+1,methodName.length()-1);
                logger.debug("paramsList: " + paramsList);
                String[] paramsArray = paramsList.split(",");

                // build out the paramTypes and args
                paramTypes = new Class<?>[paramsArray.length];
                paramArgs = new Object[paramsArray.length];

                // I had to allow for parameters only for randomDouble since there's no parameterless method to get a double value...
                try {
                    Integer.parseInt(paramsArray[0]);
                    // we have int params...
                    int i = 0;
                    for (String s: paramsArray) {
                        logger.debug("paramsArray value: " + s);
                        paramTypes[i] = int.class;
                        paramArgs[i] = Integer.parseInt(s);
                        i++;
                    }
                } catch (Exception ex) {
                    // No other params other than int are allowed...
                    throw ex;
                }

                // reset the methodName
                methodName = methodName.substring(0, methodName.indexOf("("));
            }
            method = baseObj.getClass().getDeclaredMethod(methodName, paramTypes);
            if (cnt<methodArr.length) { // we aren't done iterating
                baseObj = method.invoke(baseObj, paramArgs);
                cnt++;
            } else { // we are the last iteration
                tempObj = method.invoke(baseObj, paramArgs);
            }
        }
        logger.debug("tempObj value: " + tempObj.toString());

        logger.debug("tempObj instance of: " + tempObj.getClass().getSimpleName());

        // Now that we have our method to get the base object ready to go...
        // Let's iterate through
        // I can't test double because there is no parameterless method to get a double value from faker
        if (tempObj instanceof String) {
            if (randomArrayCnt>1) { // insert array of strings
                List<String> s2 = new ArrayList<String>();
                for (int i=0; i<(int)randomArrayCnt; i++) {
                    s2.add(method.invoke(baseObj, paramArgs).toString());
                }
                returnObj = s2;
            } else { // insert single string
                String s = tempObj.toString(); // no need to reinvoke
                logger.debug("Random String value: " + s);
                returnObj = s;
            }
        } else if (tempObj instanceof Integer ) {
            if (randomArrayCnt > 1) { // insert array of ints
                List<Integer> s2 = new ArrayList<Integer>();
                for (int i = 0; i < (int) randomArrayCnt; i++) {
                    int x = (Integer)(method.invoke(baseObj, paramArgs));
                    s2.add(x);
                    logger.debug("Getting array of ints. Base int: " + x);
                }
                returnObj = s2;
            } else { // insert single int
                returnObj = (Integer)tempObj;
                logger.debug("base int: " + returnObj);
            }
        } else if (tempObj instanceof Long) {
            if (randomArrayCnt > 1) { // insert array of longs
                List<Long> s2 = new ArrayList<Long>();
                for (int i = 0; i < (int) randomArrayCnt; i++) {
                    Long x = (Long)(method.invoke(baseObj, paramArgs));
                    s2.add(x);
                    logger.debug("Getting array of longs. Base long: " + x);
                }
                returnObj = s2;
            } else { // insert single long
                returnObj = tempObj;
                logger.debug("base long: " + returnObj);
            }
        } else if (tempObj instanceof Double) {
            if (randomArrayCnt > 1) { // insert array of doubles
                List<Double> s2 = new ArrayList<Double>();
                for (int i = 0; i < (int) randomArrayCnt; i++) {
                    Double x = (Double)(method.invoke(baseObj, paramArgs));
                    s2.add(x);
                    logger.debug("Getting array of doubles. Base double: " + x);
                }
                returnObj = s2;
            } else { // insert single double
                returnObj = tempObj;
                logger.debug("base double: " + returnObj);
            }
        } else if (tempObj instanceof Boolean) {
            if (randomArrayCnt > 1) { // insert array of bools
                List<Boolean> s2 = new ArrayList<Boolean>();
                for (int i = 0; i < (int) randomArrayCnt; i++) {
                    Boolean x = (Boolean)(method.invoke(baseObj, paramArgs));
                    s2.add(x);
                    logger.debug("Getting array of bools. Base bool: " + x);
                }
                returnObj = s2;
            } else { // insert single bool
                returnObj = (Boolean) tempObj;
                logger.debug("base bool: " + returnObj);
            }
        } else if (tempObj instanceof Date) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (randomArrayCnt > 1) { // insert array of dates
                List<Date> s2 = new ArrayList<Date>();
                for (int i = 0; i < (int) randomArrayCnt; i++) {
                    Date x = (Date)(method.invoke(baseObj, paramArgs));
                    s2.add(x);
                    logger.debug("Getting array of dates. Base date: " + x);
                }
                returnObj = s2;
            } else { // insert single date
                returnObj = tempObj;
                logger.debug("base date: " + returnObj);
            }
            //        } else if (objMeta.has("dt")) {
//            JsonArray a = objMeta.get("dt").getAsJsonArray();
//            logger.debug("dt min/max value: " + a.get(0).toString() + "/" + a.get(1).toString());
//
//            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date minDt = formatter.parse(a.get(0).getAsString());
//            Date maxDt = formatter.parse(a.get(1).getAsString());
//
//            // Generate a random date value with min/max value of above
//            double arrayCnt = getArrayCount(a);
//            if (arrayCnt > 1) { // insert array of dts
//                List<Date> s2 = new ArrayList<Date>();
//                for (int i = 0; i < (int) arrayCnt; i++) {
//                    long random = getRandomLong(minDt.getTime(), maxDt.getTime());
////                    long random = ThreadLocalRandom.current().nextLong(minDt.getTime(), maxDt.getTime());
//                    Date date = new Date(random);
//                    s2.add(date);
//                }
//                baseObj = s2;
//            } else { // insert single dt
//                long random = getRandomLong(minDt.getTime(), maxDt.getTime());
////                long random = ThreadLocalRandom.current().nextLong(minDt.getTime(), maxDt.getTime());
//                baseObj = new Date(random);
//                logger.debug("base date: " + baseObj);
//            }
        }
        return returnObj;
    }

    /**
     * Get a sample doc based on metaStructure object
     * @return return a bson document
     */
    public Document getDoc(int workerId, int sequence) throws Exception {
        Document d = new Document();

        // add _id
        logger.debug("Faker doc worker id: " + workerId + " , sequence #: " + sequence);
        Document oid = new Document("w",workerId).append("i", sequence);
        d.append("_id", oid);

        for (Map.Entry<String, JsonElement> valueEntry : metaStructure.entrySet()) {
            logger.debug("Field Name: " + valueEntry.getKey());

            JsonObject objMeta = valueEntry.getValue().getAsJsonObject();
            logger.debug("Field metadata: " + objMeta.toString());

            if (objMeta.has("doc")) { // we are only going 1 nested doc in...
                JsonObject nestedMetaStructure = objMeta.get("doc").getAsJsonObject();
                logger.debug("Nested Object found: " + nestedMetaStructure.toString());
                // Find out if there's an array or not... this is a bit cloogey
                if (nestedMetaStructure.has("_cnt")) { // this is an array
                    int cnt = nestedMetaStructure.get("_cnt").getAsInt();
                    int randomArrayCnt = (int)getArrayCount(cnt);

                    List<Document> docs = new ArrayList<Document>();
                    for (int i=0; i<cnt; i++) {
                        Document nestedDoc = new Document();
                        for (Map.Entry<String, JsonElement> nestedValueEntry : nestedMetaStructure.entrySet()) {
                            if (!nestedValueEntry.getKey().equalsIgnoreCase("_cnt")) { // skip _cnt
                                JsonObject nestedObjectMeta = nestedValueEntry.getValue().getAsJsonObject();
                                logger.debug("- Nested Field Name: " + nestedValueEntry.getKey());
                                nestedDoc.append(nestedValueEntry.getKey(), getBaseObject(nestedObjectMeta));
                            }
                        }
                        docs.add(nestedDoc);
                    }
                    d.append(valueEntry.getKey(), docs);
                } else {
                    Document nestedDoc = new Document();
                    for (Map.Entry<String, JsonElement> nestedValueEntry : nestedMetaStructure.entrySet()) {
                        JsonObject nestedObjectMeta = nestedValueEntry.getValue().getAsJsonObject();
                        logger.debug("- Nested Field Name: " + nestedValueEntry.getKey());
                        nestedDoc.append(nestedValueEntry.getKey(), getBaseObject(nestedObjectMeta));
                    }
                    d.append(valueEntry.getKey(), nestedDoc);
                }
            } else { // a faker object of some sort
                d.append(valueEntry.getKey(), getBaseObject(objMeta));
            }

//            if (objMeta.has("str") || objMeta.has("int") || objMeta.has("dt")) {
//                d.append(valueEntry.getKey(), getBaseObject(objMeta));
//            } else if (objMeta.has("doc")) { // we are only going 1 nested doc in...
//                JsonObject nestedMetaStructure = objMeta.get("doc").getAsJsonObject();
//                logger.debug("Nested Object found: " + nestedMetaStructure.toString());
//                Document nestedDoc = new Document();
//                for (Map.Entry<String, JsonElement> nestedValueEntry : nestedMetaStructure.entrySet()) {
//                    JsonObject nestedObjectMeta = nestedValueEntry.getValue().getAsJsonObject();
//                    logger.debug("- Nested Field Name: " + nestedValueEntry.getKey());
//                    if (nestedObjectMeta.has("str")
//                            || nestedObjectMeta.has("int")
//                            || nestedObjectMeta.has("dt") ) {
//                        logger.debug("- Getting nested based object...");
//                        nestedDoc.append(nestedValueEntry.getKey(), getBaseObject(nestedObjectMeta));
//                    }
//                }
//                d.append(valueEntry.getKey(), nestedDoc);
//            }
        }
        logger.debug("Returning Sample Doc: " + d.toJson());
        return d;
    }

    public static void main(String[] args)
            throws Exception {
        logger.debug("Testing faker metastructure parsing");

        String name = faker.name().fullName(); // Miss Samanta Schmidt
        String firstName = faker.name().firstName(); // Emory
        String lastName = faker.name().lastName(); // Barton
        Boolean bool = faker.bool().bool(); // Barton
        Double dbl = faker.number().randomDouble(3, 0, 10);
        System.out.println(dbl);
        dbl = faker.number().randomDouble(3, 0, 10);
        System.out.println(dbl);
        Date dt = faker.date().birthday();
        System.out.println(dt.toString());

        String streetAddress = faker.address().streetAddress(); // 60018 Sawayn Brooks Suite 449

        Method method = faker.getClass().getDeclaredMethod("address", null);
        Object obj = method.invoke(faker, null);
        method = obj.getClass().getDeclaredMethod("streetAddress", null);
        obj = method.invoke(obj, null);

        if (obj instanceof Object) {
            System.out.println("Object returned: " + obj.toString());
        } else {
            System.out.println("Non-Object returned");
        }

        System.out.println(name);
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(bool);
        System.out.println(streetAddress);

        name = faker.name().fullName(); // Miss Samanta Schmidt
        System.out.println(name);

        StringBuilder sampleMetastructureJson = new StringBuilder();
        sampleMetastructureJson.append("{");
        sampleMetastructureJson.append("multiaddress: {\"address.streetAddress\":3}, ");
        sampleMetastructureJson.append("singleaddress: {\"address.streetAddress\":1}, ");
        sampleMetastructureJson.append("singleint: {\"number.randomDigit\":1}, ");
        sampleMetastructureJson.append("multiint: {\"number.randomDigit\":3}, ");
        sampleMetastructureJson.append("singlelong: {\"number.randomNumber\":1}, ");
        sampleMetastructureJson.append("multilong: {\"number.randomNumber\":3}, ");
        sampleMetastructureJson.append("singlebool: {\"bool.bool\":1}, ");
        sampleMetastructureJson.append("multibool: {\"bool.bool\":3}, ");
        sampleMetastructureJson.append("singledbl: {\"number.randomDouble(3,0,100)\":1}, ");
        sampleMetastructureJson.append("multidbl: {\"number.randomDouble(3,0,100)\":5}, ");
        sampleMetastructureJson.append("singledt: {\"date.birthday\":1}, ");
        sampleMetastructureJson.append("multidt: {\"date.birthday\":3}, ");
        sampleMetastructureJson.append("phones: {doc:{type:{\"number.randomDigit\":1}, val:{\"phoneNumber.phoneNumber\":1}}}, ");
        sampleMetastructureJson.append("productsArray: {doc:{nm:{\"commerce.productName\":1}, price:{\"commerce.price\":1}, _cnt:3}} ");
        sampleMetastructureJson.append("}");
        logger.debug("sampleMetastructureJson: " + sampleMetastructureJson.toString());

        String[] pocArgs = new String[2];
        pocArgs[0] = "-fakertemplate";
        pocArgs[1] = sampleMetastructureJson.toString();
        POCTestOptions o = new POCTestOptions(pocArgs);

        FakerRecord fr = FakerRecord.getInstance(o);

        logger.debug("Sample Doc: " + fr.getDoc(1, 1).toJson());

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        MongoDatabase db = mongoClient.getDatabase("temp");
        MongoDatabase admindb = mongoClient.getDatabase("admin");
        Document d = admindb.runCommand(new Document("serverStatus", 1));
        logger.debug(d.toJson());

        MongoIterable<String> collection = db.listCollectionNames();
        MongoCursor<String> cursor = collection.iterator();
        while(cursor.hasNext()){
            String table = cursor.next();
            logger.debug(table);
        }

        MongoCollection<Document> coll = db.getCollection("richquery");

        coll.insertOne(fr.getDoc(1, 1));
        System.exit(1);


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
