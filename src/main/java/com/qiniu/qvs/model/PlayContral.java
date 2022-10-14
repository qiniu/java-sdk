package com.qiniu.qvs.model;

public class PlayContral {
    private String command;
    private String range;
    private Float scale;

    public PlayContral(String command, String range, Float scale) {
        this.command = command;
        this.range = range;
        this.scale = scale;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public Float getScale() {
        return scale;
    }

    public void setScale(Float scale) {
        this.scale = scale;
    }
}
