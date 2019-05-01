package com.johnlpage.pocdriver.objects;


import org.apache.commons.cli.CommandLine;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


//Yes - lots of public values, getters are OTT here.

public class POCTestOptions {
	private static final Logger logger = LogManager.getLogger(POCTestOptions.class);

	public int batchSize = 512;
	public int numFields = 10;
	public int depth = 0;
	public final long NUMBER_SIZE = 1000000;
	public int textFieldLen = 30;
	public int numThreads = 4;
	public int threadIdStart = 0;
	public int reportTime = 10;
	public int slowThreshold = 50;
	public int insertops = 100;
	public int opsPerSecond = 0;
	public int keyqueries = 0;
	public int arrayupdates = 0;
	public int updates = 0;
	public int rangequeries=0;
	public int duration = 18000;
	public int numShards = 1;
	public String logfile = null;
	public boolean sharded = false;
	public boolean singleserver = false;
//	private String statsfile = "pocload.csv";
	public String databaseName = "POCDB";
	public String collectionName = "POCCOLL";
	public String workflow = null;
	public boolean emptyFirst = false;
	public boolean printOnly = false;
	public int secondaryidx=0;
	public int arraytop = 0;
	public int arraynext = 0;
	public int numcollections = 1;
	public int rangeDocs=10;
	public int updateFields=1;
	public int projectFields=0;
	public String customtemplate = null;
	public String fakertemplate = null;
	public String customagg = null;

	/**
	 * Control whether we show full stacktraces on error
	 */
	public boolean debug=false;

	//Zipfian stuff
	public boolean zipfian = false;
	public int zipfsize = 0;

    public int blobSize = 0;

	public boolean findandmodify=false;
	public int workingset = 100;
	public boolean helpOnly = false;
	public String connectionDetails = "mongodb://localhost:27017";
	public boolean fulltext;
	
	public POCTestOptions(String[] args) throws ParseException
	{
		CommandLineParser parser = new DefaultParser();
		
		Options cliopt;
		cliopt = new Options();
		cliopt.addOption("a","arrays",true,"Shape of any arrays in new sample records x:y so -a 12:60 adds an array of 12 length 60 arrays of integers");
		cliopt.addOption("b","bulksize",true,"Bulk op size (default 512)");
		cliopt.addOption("c","host",true,"Mongodb connection details (default 'mongodb://localhost:27017' )");
		cliopt.addOption("d","duration",true,"Test duration in seconds, default 18,000");
		cliopt.addOption("e","empty",false,"Remove data from collection on startup");
		cliopt.addOption("f","numfields",true,"Number of top level fields in test records (default 10)");
		cliopt.addOption(null,"depth",true,"The depth of the document created (default 0)");
		cliopt.addOption("g","arrayupdates",true,"Ratio of array increment ops requires option 'a' (default 0)");
		cliopt.addOption("h","help",false,"Show Help");
		cliopt.addOption("i","inserts",true,"Ratio of insert operations (default 100)");
		cliopt.addOption("j","workingset",true,"Percentage of database to be the working set (default 100)");
		cliopt.addOption("k","keyqueries",true,"Ratio of key query operations (default 0)");
		cliopt.addOption("l","textfieldsize",true,"Length of text fields in bytes (default 30)");
		cliopt.addOption("m","findandmodify",false,"Use findAndModify instead of update and retrieve record (with -u or -v only)");
		cliopt.addOption("n","namespace",true,"Namespace to use , for example myDatabase.myCollection");
		cliopt.addOption("o","logfile",true,"Output stats to  <file> ");
		cliopt.addOption("p","print",false,"Print out a sample record according to the other parameters then quit");
		cliopt.addOption("q","opsPerSecond",true,"Try to rate limit the total ops/s to the specified amount");
		cliopt.addOption("r","rangequeries",true,"Ratio of range query operations (default 0)");
		cliopt.addOption("s","slowthreshold",true,"Slow operation threshold in ms(default 50)");
		cliopt.addOption("t","threads",true,"Number of threads (default 4)");
		cliopt.addOption("u","updates",true,"Ratio of update operations (default 0)");
		cliopt.addOption("v","workflow",true,"Specify a set of ordered operations per thread from [iukpa]. 'a' is a new param to run aggregation queries that don't use index keys since the data is random. customagg param is req'd for this param.");
		cliopt.addOption("w","nosharding",false,"Do not shard the collection");
		cliopt.addOption("x","indexes",true,"Number of secondary indexes - does not remove existing (default 0)");
		cliopt.addOption("y","collections",true,"Number of collections to span the workload over, implies w (default 1)");
		cliopt.addOption("z","zipfian",true,"Enable zipfian distribution over X number of documents (default 0)");
		cliopt.addOption(null,"threadIdStart",true,"Start 'workerId' for each thread. 'w' value in _id. (default 0)");
		cliopt.addOption(null,"fulltext",false,"Create fulltext index (default false)");
		cliopt.addOption(null,"binary",true,"add a binary blob of size KB");
		cliopt.addOption(null,"rangedocs",true,"Number of documents to fetch for range queries (default 10)");
		cliopt.addOption(null,"updatefields",true,"Number of fields to update (default 1)");
		cliopt.addOption(null,"projectfields",true,"Number of fields to project in finds (default 0, which is no projection)");
		cliopt.addOption(null,"debug",false,"Show more detail if exceptions occur during inserts/queries");
		cliopt.addOption(null,"customtemplate",true,"Provide a custom document template. Custom queries is required for custom templates.");
		cliopt.addOption(null,"fakertemplate",true,"Provide a faker document template. Custom queries is required for faker templates.");
		cliopt.addOption(null,"customagg",true,"Provide a custom aggregation query in the form of pipe-delimited json docs for each stage of the agg pipeline");

		CommandLine cmd = parser.parse(cliopt, args);

		if(cmd.hasOption("binary"))
		{
			blobSize = Integer.parseInt(cmd.getOptionValue("binary"));
		}
		
		if(cmd.hasOption("q"))
		{
			opsPerSecond = Integer.parseInt(cmd.getOptionValue("q"));
		}
		
		if(cmd.hasOption("j"))
		{
			workingset = Integer.parseInt(cmd.getOptionValue("j"));
		}
		
		if(cmd.hasOption("v"))
		{
			workflow = cmd.getOptionValue("v");
		}	
		
		if(cmd.hasOption("n"))
		{
			String ns = cmd.getOptionValue("n");
			String[] parts = ns.split("\\.");
			if(parts.length != 2)
			{
				System.err.println("namespace format is 'DATABASE.COLLECTION' ");
				System.exit(1);
			}
			databaseName=parts[0];
			collectionName=parts[1];
		}
		
		if(cmd.hasOption("a"))
		{
			String ao = cmd.getOptionValue("a");
			String[] parts = ao.split(":");
			if(parts.length != 2)
			{
				System.err.println("array format is 'top:second'");
				System.exit(1);
			}
			arraytop = Integer.parseInt(parts[0]);
			arraynext = Integer.parseInt(parts[1]);
		}
		
		if(cmd.hasOption("e"))
		{
			emptyFirst=true;
		}
		
		
		if(cmd.hasOption("p"))
		{
			printOnly=true;
		}
		
		if(cmd.hasOption("w"))
		{
			singleserver=true;
		}
		if(cmd.hasOption("r"))
		{
			rangequeries = Integer.parseInt(cmd.getOptionValue("r"));
		}
		
		if(cmd.hasOption("d"))
		{
			duration = Integer.parseInt(cmd.getOptionValue("d"));
		}
		
		if(cmd.hasOption("g"))
		{
			arrayupdates = Integer.parseInt(cmd.getOptionValue("g"));
		}
		
		if(cmd.hasOption("u"))
		{
			updates = Integer.parseInt(cmd.getOptionValue("u"));
		}
		
		if(cmd.hasOption("i"))
		{
			insertops = Integer.parseInt(cmd.getOptionValue("i"));
		}
		
		if(cmd.hasOption("x"))
		{
			secondaryidx = Integer.parseInt(cmd.getOptionValue("x"));
		}
		if(cmd.hasOption("y"))
		{
			numcollections = Integer.parseInt(cmd.getOptionValue("y"));
			singleserver=true;
		}
		if(cmd.hasOption("z"))
		{
			zipfian = true;
			zipfsize = Integer.parseInt(cmd.getOptionValue("z"));
		}
		
		if(cmd.hasOption("o"))
		{
			logfile = cmd.getOptionValue("o");
		}
		
		if(cmd.hasOption("k"))
		{
			keyqueries = Integer.parseInt(cmd.getOptionValue("k"));
		}
		
		if(cmd.hasOption("b"))
		{
			batchSize = Integer.parseInt(cmd.getOptionValue("b"));
		}
		
		if(cmd.hasOption("s"))
		{
			slowThreshold = Integer.parseInt(cmd.getOptionValue("s"));
		}
		
		// automatically generate the help statement
		if(cmd.hasOption("h"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "POCDriver", cliopt );
			helpOnly = true;
		}
		
		if(cmd.hasOption("c"))
		{
			connectionDetails = cmd.getOptionValue("c");
		}	
			
		if(cmd.hasOption("l"))
		{
			textFieldLen = Integer.parseInt(cmd.getOptionValue("l"));
		}
		
		if(cmd.hasOption("m"))
		{
			findandmodify = true;
		}
		
		if(cmd.hasOption("f"))
		{
			numFields = Integer.parseInt(cmd.getOptionValue("f"));
		}

		if(cmd.hasOption("depth"))
		{
			depth = Integer.parseInt(cmd.getOptionValue("depth"));
		}

//		if(cmd.hasOption("o"))
//		{
//			statsfile = cmd.getOptionValue("o");
//		}
		
		if(cmd.hasOption("t"))
		{
			numThreads = Integer.parseInt(cmd.getOptionValue("t"));
		}
		if(cmd.hasOption("fulltext"))
        {
            fulltext = true;
        }
		
		if(cmd.hasOption("threadIdStart"))
		{
			threadIdStart = Integer.parseInt(cmd.getOptionValue("threadIdStart"));
		}

		if(cmd.hasOption("rangedocs"))
		{
			rangeDocs = Integer.parseInt(cmd.getOptionValue("rangedocs"));
		}

		if(cmd.hasOption("updatefields"))
		{
			updateFields = Integer.parseInt(cmd.getOptionValue("updatefields"));
		}

		if(cmd.hasOption("projectfields"))
		{
			projectFields = Integer.parseInt(cmd.getOptionValue("projectfields"));
		}

		if(cmd.hasOption("debug"))
		{
			debug = true;
		}

		if(cmd.hasOption("customtemplate"))
		{
			logger.debug("Custom Template: " + cmd.getOptionValue("customtemplate"));
			customtemplate = cmd.getOptionValue("customtemplate");
		}

		if(cmd.hasOption("fakertemplate"))
		{
			logger.debug("Faker Template: " + cmd.getOptionValue("fakertemplate"));
			fakertemplate = cmd.getOptionValue("fakertemplate");
		}

		if(cmd.hasOption("customagg"))
		{
			logger.debug("Custom Agg Query: " + cmd.getOptionValue("customagg"));
			customagg = cmd.getOptionValue("customagg");
		}
	}
}
