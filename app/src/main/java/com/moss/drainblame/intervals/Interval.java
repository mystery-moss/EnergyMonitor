package com.moss.drainblame.intervals;

import com.moss.drainblame.apps.App;

/*
 *  Information collected in one battery interval
 */

public class Interval {
    public final int level;       //Battery level at *end* of interval
    public final long length;
    public final long screenOnTime;
    public final long networkBytes;
    public App[] activeApps;

    public Interval(int level, long length, long screenOnTime, long networkBytes, App[] activeApps){
        this.level = level;
        this.length = length;
        this.screenOnTime = screenOnTime;
        this.networkBytes = networkBytes;
        this.activeApps = activeApps;
    }
}