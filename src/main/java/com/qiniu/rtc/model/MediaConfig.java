package com.qiniu.rtc.model;

import java.io.Serializable;

public class MediaConfig implements Serializable {

    private static final long serialVersionUID = -2781620162179559305L;

    /**
     * 输出帧率
     */
    private int fps;

    /**
     * 输出码率
     */
    private int kbps;

    /**
     * 指定输出宽度 必须为偶数
     */
    private int width;

    /**
     * 指定输出高度 必须为偶数
     */
    private int height;

    /**
     * 可选 是否保留用户最后一帧 默认false
     */
    private boolean holdLastFrame;

    /**
     * 可选 是否仅仅做音频合流
     */
    private boolean audioOnly;

    /**
     * 可选 指定拉伸模式 默认为 aspectFill
     */
    private StretchModeEnum stretchMode;

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getKbps() {
        return kbps;
    }

    public void setKbps(int kbps) {
        this.kbps = kbps;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHoldLastFrame() {
        return holdLastFrame;
    }

    public void setHoldLastFrame(boolean holdLastFrame) {
        this.holdLastFrame = holdLastFrame;
    }

    public boolean isAudioOnly() {
        return audioOnly;
    }

    public void setAudioOnly(boolean audioOnly) {
        this.audioOnly = audioOnly;
    }

    public StretchModeEnum getStretchMode() {
        return stretchMode;
    }

    public void setStretchMode(StretchModeEnum stretchMode) {
        this.stretchMode = stretchMode;
    }

    @Override
    public String toString() {
        return "MediaConfig{"
                + "fps=" + fps
                + ", kbps=" + kbps
                + ", width=" + width
                + ", height=" + height
                + ", holdLastFrame=" + holdLastFrame
                + ", audioOnly=" + audioOnly
                + ", stretchMode=" + stretchMode
                + '}';
    }
}
