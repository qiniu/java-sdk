package com.qiniu.rtc.model;

import java.io.Serializable;

public class MediaInput implements Serializable {

    private static final long serialVersionUID = -9162560559667684764L;

    /**
     * 可选，默认为media类型。
     * 选值范围 image/media，media时 userId必须，image时imageUrl必填
     */
    private String kind;

    /**
     * kind == media 必填，用户加入房间时用户名
     */
    private String userId;

    /**
     * kind == image 必填，图片地址。
     */
    private String url;

    /**
     * 可选，发布多路时候用于区分不同输入
     */
    private String tag;

    /**
     * 可选，任务为basic并且多路进行合流时候必填
     */
    private MediaPosition position;

    /**
     * 可选，拉伸模式 默认为 aspectFill
     */
    private StretchModeEnum stretchMode;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }


    public StretchModeEnum getStretchMode() {
        return stretchMode;
    }

    public void setStretchMode(StretchModeEnum stretchMode) {
        this.stretchMode = stretchMode;
    }

    public MediaPosition getPosition() {
        return position;
    }

    public void setPosition(MediaPosition position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "MediaInput{"
                + "kind='" + kind + '\''
                + ", userId='" + userId + '\''
                + ", url='" + url + '\''
                + ", tag='" + tag + '\''
                + ", position=" + position
                + ", stretchMode=" + stretchMode
                + '}';
    }
}
