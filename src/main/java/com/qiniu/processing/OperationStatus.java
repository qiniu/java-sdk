package com.qiniu.processing;

/**
 * Created by jemy on 06/02/2017.
 *
 * @link https://developer.qiniu.com/dora/api/persistent-processing-status-query-prefop
 */
public class OperationStatus {
    private String id;
    private int code;
    private String desc;
    private String inputKey;
    private String inputBucket;
    private String pipeline;
    private String reqid;
    private OperationResult[] items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getInputKey() {
        return inputKey;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public String getInputBucket() {
        return inputBucket;
    }

    public void setInputBucket(String inputBucket) {
        this.inputBucket = inputBucket;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public String getReqid() {
        return reqid;
    }

    public void setReqid(String reqid) {
        this.reqid = reqid;
    }

    public OperationResult[] getItems() {
        return items;
    }

    public void setItems(OperationResult[] items) {
        this.items = items;
    }

    public class OperationResult {
        private String cmd;
        private int code;
        private String desc;
        private String error;
        private String hash;
        private String key;
        private int returnOld;

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getReturnOld() {
            return returnOld;
        }

        public void setReturnOld(int returnOld) {
            this.returnOld = returnOld;
        }
    }
}
