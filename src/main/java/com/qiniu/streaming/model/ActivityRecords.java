package com.qiniu.streaming.model;

/**
 * Created by bailong on 16/9/22.
 */
public final class ActivityRecords {
    public Item[] items;

    public static class Item {
        public long start;
        public long end;
    }
}
