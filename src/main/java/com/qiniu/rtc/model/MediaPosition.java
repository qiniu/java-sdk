package com.qiniu.rtc.model;

import java.io.Serializable;

public class MediaPosition implements Serializable {

    private static final long serialVersionUID = 3616082871842887188L;

    private int x;

    private int y;

    /**
     * 必须为偶数
     */
    private int w;

    /**
     * 必须为偶数
     */
    private int h;

    /**
     * 可选 0 ~ 65535
     */
    private int z;

    public MediaPosition() {
    }

    public MediaPosition(int x, int y, int w, int h, int z) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "MediaPosition{"
                + "x=" + x
                + ", y=" + y
                + ", w=" + w
                + ", h=" + h
                + ", z=" + z
                + '}';
    }
}
