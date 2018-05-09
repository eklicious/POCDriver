package com.johnlpage.pocdriver.objects;


import java.util.concurrent.atomic.AtomicLong;


class POCopStats {
    AtomicLong intervalCount;
    AtomicLong totalOpsDone;
    AtomicLong slowOps;

    POCopStats() {
        intervalCount = new AtomicLong(0);
        totalOpsDone = new AtomicLong(0);
        slowOps = new AtomicLong(0);
    }
}
