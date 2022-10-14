package com.qiniu.qvs.model;

public class VoiceChat {
    private Boolean latency; // 该字段为true时，启用低延迟版本，收到返回地址后在发送语音数据
    private String[] channels; // 平台设备指定需要启动的通道国标ID（为空表示启动平台下的所有设备）
    private String version; // 对讲国标协议版本，取值"2014"或"2016"，默认为2014，例如大部分大华摄像头为GBT 28181-2014版本对讲模式
    private String transProtocol; // 取值"tcp"或"udp"，流传输模式，默认udp

    public VoiceChat(Boolean latency, String[] channels, String version, String transProtocol) {
        this.latency = latency;
        this.channels = channels;
        this.version = version;
        this.transProtocol = transProtocol;
    }

    public Boolean getLatency() {
        return latency;
    }

    public void setLatency(Boolean latency) {
        this.latency = latency;
    }

    public String[] getChannels() {
        return channels;
    }

    public void setChannels(String[] channels) {
        this.channels = channels;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTransProtocol() {
        return transProtocol;
    }

    public void setTransProtocol(String transProtocol) {
        this.transProtocol = transProtocol;
    }
}
