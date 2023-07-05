package com.qiniu.util;

public class Timestamp {
    public static long second() {
        return System.currentTimeMillis() / 1000;
    }

    public static long milliSecond() {
        return System.currentTimeMillis();
    }
}
