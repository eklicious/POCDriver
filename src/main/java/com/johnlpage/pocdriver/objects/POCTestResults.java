package com.johnlpage.pocdriver.objects;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class POCTestResults {

    private static final Logger logger = LogManager.getLogger(POCTestResults.class);

    /**
     * The time this LoadRunner started
     */
    private Date startTime;
    private Date lastIntervalTime;
    private long initialCount;

    public static String[] opTypes = {"inserts", "keyqueries", "updates", "rangequeries", "aggqueries"};
    private ConcurrentHashMap<String, POCopStats> opStats;


    public POCTestResults() {
        startTime = new Date();
        lastIntervalTime = new Date();
        opStats = new ConcurrentHashMap<String, POCopStats>();

        for (String s : opTypes) {
            opStats.put(s, new POCopStats());
        }
    }

    //This returns inserts per second since we last called it
    //Rather than us keeping an overall figure

    public HashMap<String, Long> GetOpsPerSecondLastInterval() {

        HashMap<String, Long> rval = new HashMap<String, Long>();

        Date now = new Date();
        Long milliSecondsSinceLastCheck = now.getTime() - lastIntervalTime.getTime();

        for (String s : opTypes) {
            Long opsNow = GetOpsDone(s);
            Long opsPrev = GetPrevOpsDone(s);
            Long opsPerInterval = ((opsNow - opsPrev) * 1000) / milliSecondsSinceLastCheck;
            rval.put(s, opsPerInterval);
            SetPrevOpsDone(s, opsNow);
        }

        lastIntervalTime = now;

        return rval;
    }

    public Long GetSecondsElapsed() {
        Date now = new Date();
        return (now.getTime() - startTime.getTime()) / 1000;
    }


    private Long GetPrevOpsDone(String opType) {
        POCopStats os = opStats.get(opType);
        return os.intervalCount.get();
    }

    private void SetPrevOpsDone(String opType, Long numOps) {
        POCopStats os = opStats.get(opType);
        os.intervalCount.set(numOps);
    }

    public Long GetOpsDone(String opType) {
        POCopStats os = opStats.get(opType);
        return os.totalOpsDone.get();
    }


    public Long GetSlowOps(String opType) {
        POCopStats os = opStats.get(opType);
        return os.slowOps.get();
    }

    public void RecordSlowOp(String opType, int number) {
        POCopStats os = opStats.get(opType);
        os.slowOps.addAndGet(number);
    }

    public void RecordOpsDone(String opType, int howmany) {
        POCopStats os = opStats.get(opType);
        if (os == null) {
            logger.debug("Cannot fetch opstats for " + opType);
        } else {
            os.totalOpsDone.addAndGet(howmany);
        }
    }

    public void setInitialCount(long cnt) {
        this.initialCount = cnt;
    }

    public long getInitialCount() {
        return this.initialCount;
    }

}
