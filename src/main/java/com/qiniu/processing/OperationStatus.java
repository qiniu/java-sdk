package com.qiniu.processing;

/**
 * Created by jemy on 06/02/2017.
 *
 * @link https://developer.qiniu.com/dora/api/persistent-processing-status-query-prefop
 */
public class OperationStatus {
    public String id;
    public int code;
    public String desc;
    public String inputKey;
    public String inputBucket;
    public String pipeline;
    public String reqid;
    public OperationResult[] items;

    public class OperationResult {
        public String cmd;
        public int code;
        public String desc;
        public String error;
        public String hash;
        public String key;
        public int returnOld;
    }
}
