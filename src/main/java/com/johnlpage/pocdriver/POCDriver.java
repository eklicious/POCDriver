package com.johnlpage.pocdriver;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.johnlpage.pocdriver.objects.CustomRecord;
import com.johnlpage.pocdriver.objects.POCTestOptions;
import com.johnlpage.pocdriver.objects.POCTestResults;
import com.johnlpage.pocdriver.objects.TestRecord;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Logger;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

import java.util.logging.LogManager;

public class POCDriver {
    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(POCDriver.class);

    public static void main(String[] args) throws java.text.ParseException {

        POCTestOptions testOpts;
        LogManager.getLogManager().reset();
        logger.info("MongoDB Proof Of Concept - Load Generator");
        try {
            testOpts = new POCTestOptions(args);
            // Quit after displaying help message
            if (testOpts.helpOnly) {
                return;
            }

            if (testOpts.arrayupdates > 0 && (testOpts.arraytop < 1 || testOpts.arraynext < 1)) {
                logger.error("You must specify an array size to update arrays");
                return;
            }
            if (testOpts.printOnly) {
                printTestDocument(testOpts);
                return;
            }

        } catch (ParseException e) {
            logger.error(e.getMessage());
            return;
        }

        POCTestResults testResults = new POCTestResults();
        LoadRunner runner = new LoadRunner(testOpts);
        runner.RunLoad(testOpts, testResults);
    }

    /**
     * EK: updated this to use either TestRecord or CustomRecord
     * @param testOpts all options for this load driver
     */
    private static void printTestDocument(final POCTestOptions testOpts) throws java.text.ParseException {
        //Sets up sample data don't remove
        int[] arr = new int[2];
        arr[0] = testOpts.arraytop;
        arr[1] = testOpts.arraynext;

        Document d;

        if (testOpts.customtemplate!=null) { // use CustomRecord
            CustomRecord cr = CustomRecord.getInstance(testOpts);
            d = cr.getDoc(0, 0);
        } else {
            d = (new TestRecord(testOpts.numFields, testOpts.depth, testOpts.textFieldLen,
                    1, 12345678, testOpts.NUMBER_SIZE, arr, testOpts.blobSize)).internalDoc;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(d.toJson());

        String json = gson.toJson(je);
        StringBuilder newJson = new StringBuilder();
        int arrays = 0;

        // Collapse inner newlines
        boolean inquotes = false;
        for (int c = 0; c < json.length(); c++) {
            char inChar = json.charAt(c);
            if (inChar == '[') {
                arrays++;
            }
            if (inChar == ']') {
                arrays--;
            }
            if (inChar == '"') {
                inquotes = !inquotes;
            }

            if (arrays > 1 && inChar == '\n') {
                continue;
            }
            if (arrays > 1 && !inquotes && inChar == ' ') {
                continue;
            }
            newJson.append(json.charAt(c));
        }

        logger.info(newJson.toString());

        //Thanks to Ross Lawley for this bit of black magic
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonBinaryWriter binaryWriter = new BsonBinaryWriter(buffer);
        new DocumentCodec().encode(binaryWriter, d, EncoderContext.builder().build());
        int length = binaryWriter.getBsonOutput().getSize();

        logger.info(String.format("Records are %.2f KB each as BSON", (float) length / 1024));
    }

}
