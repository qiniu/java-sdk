package com.qiniu.storage.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Bucket信息
 */
public class BucketInfo {

    /**
     * 可以有多个，以;分隔，如果前一个回源地址返回5XX，则会重试下一个。
     */
    @SerializedName("source")
    private String imageSource;

    /**
     * 请求镜像地址时携带的 Host 头部
     */
    @SerializedName("host")
    private String imageHost;

    /**
     * 是否开启原图保护
     */
    // CHECKSTYLE:OFF
    @SerializedName("protected")
    private int _protected;

    /**
     * 是否是私有空间
     */
    @SerializedName("private")
    private int _private;

    /**
     * 开启后, 空间根目录中的 index.html（或 index.htm）文件将会作为默认首页进行展示
     */
    // CHECKSTYLE:ON
    @SerializedName("no_index_page")
    private int noIndexPage;

    /**
     * imgsft是镜像回源容错保护机制，防止镜像回源拉到异常文件
     */
    @SerializedName("imgsft")
    private int imgsft;

    /**
     * Bucket的cache-control: max-age属性
     */
    @SerializedName("max_age")
    private int maxAge;

    /**
     * 是字符串，如"-"，如果有多个，就按字符串形式写在一起，如"-!/"
     */
    @SerializedName("separator")
    private String separator;

    /**
     * styles: {
     * StyleName1 : Style1,
     * ..}
     * <br>
     * StyleName1: FOP样式名称<br>
     * Style1 StyleName1: 对应的具体FOP指令及参数
     */
    @SerializedName("styles")
    private Map<String, String> styles;

    /**
     * 含义同Region, 兼容字段
     */
    @SerializedName("zone")
    private String zone;

    /**
     * 存储区域
     */
    @SerializedName("region")
    private String region;

    /**
     * 是否为全局域名
     */
    @SerializedName("global")
    private boolean global;

    /**
     * 防盗链模式, 0: 表示关闭；1: 表示设置Referer白名单; 2: 表示设置Referer黑名单
     */
    @SerializedName("anti_leech_mode")
    private int antiLeechMode;

    /**
     * 防盗链白名单
     */
    @SerializedName("refer_wl")
    private String[] referWhite;

    /**
     * 防盗链黑名单
     */
    @SerializedName("refer_bl")
    private String[] referBlack;

    /**
     * 是否允许空 Refer 访问, 0: 表示不允许; 1: 表示允许
     */
    @SerializedName("no_refer")
    private boolean noRefer;

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
        return _protected;
    }

    public void setProtected(int _protected) {
        this._protected = _protected;
    }

    public int getPrivate() {
        return _private;
    }

    public void setPrivate(int _private) {
        this._private = _private;
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

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
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

    public int getAntiLeechMode() {
        return antiLeechMode;
    }

    public void setAntiLeechMode(int antiLeechMode) {
        this.antiLeechMode = antiLeechMode;
    }

    public String[] getReferWhite() {
        return referWhite;
    }

    public void setReferWhite(String[] referWhite) {
        this.referWhite = referWhite;
    }

    public String[] getReferBlack() {
        return referBlack;
    }

    public void setReferBlack(String[] referBlack) {
        this.referBlack = referBlack;
    }

    public boolean isNoRefer() {
        return noRefer;
    }

    public void setNoRefer(boolean noRefer) {
        this.noRefer = noRefer;
    }

}
