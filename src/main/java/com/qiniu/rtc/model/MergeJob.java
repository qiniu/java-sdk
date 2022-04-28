package com.qiniu.rtc.model;

import java.io.Serializable;
import java.util.List;

public class MergeJob implements Serializable {

    private static final long serialVersionUID = -6100722396626298717L;

    private String id;

    /**
     * 固定 现在仅支持 basic
     */
    private String type;

    /**
     * 输入媒体信息
     */
    private List<MediaInput> inputs;

    /**
     * 设置推流地址，现仅支持单地址
     */
    private List<MediaOutput> outputs;

    /**
     * 合流任务设置。
     */
    private MediaConfig config;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<MediaInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<MediaInput> inputs) {
        this.inputs = inputs;
    }

    public List<MediaOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<MediaOutput> outputs) {
        this.outputs = outputs;
    }

    public MediaConfig getConfig() {
        return config;
    }

    public void setConfig(MediaConfig config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "MergeJob{"
                + "id='" + id + '\''
                + ", type='" + type + '\''
                + ", inputs=" + inputs
                + ", outputs=" + outputs
                + ", config=" + config
                + '}';
    }
}
