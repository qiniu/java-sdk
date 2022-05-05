package com.qiniu.rtc.model;

import java.io.Serializable;

public class MediaOutput implements Serializable {

    private static final long serialVersionUID = 5010528238721566857L;

    /**
     * 现仅支持 rtmp
     */
    private String type;

    /**
     * rtmp地址, example: rtmp://qiniu.com/stream
     */
    private String url;

    public MediaOutput() {
    }

    public MediaOutput(String type, String url) {
        this.type = type;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "MediaOutput{"
                + "type='" + type + '\''
                + ", url='" + url + '\''
                + '}';
    }
}
