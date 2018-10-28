package com.qiniu.storage.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class BucketInfo {
    @SerializedName("source")
    private String imageSource;
    @SerializedName("host")
    private String imageHost;
    @SerializedName("protected")
    private int beProtected;
    @SerializedName("private")
    private int bePrivate;
    @SerializedName("no_index_page")
    private int noIndexPage;
    private int imgsft;
    private String separator;
    private Map<String, String> styles;
    private String zone;
    private String region;
    private boolean global;

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public int getProtected() {
        return beProtected;
    }

    public void setProtected(int beProtected) {
        this.beProtected = beProtected;
    }

    public int getPrivate() {
        return bePrivate;
    }

    public void setPrivate(int bePrivate) {
        this.bePrivate = bePrivate;
    }

    public int getNoIndexPage() {
        return noIndexPage;
    }

    public void setNoIndexPage(int noIndexPage) {
        this.noIndexPage = noIndexPage;
    }

    public int getImgsft() {
        return imgsft;
    }

    public void setImgsft(int imgsft) {
        this.imgsft = imgsft;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public Map<String, String> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, String> styles) {
        this.styles = styles;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }
}
