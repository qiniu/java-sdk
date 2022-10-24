package com.qiniu.rtc.model;

import lombok.Data;

@Data
public class AppParam {
    private String appId;
    private String hub;
    private String title;
    private int maxUsers;
    private boolean noAutoKickUser;
}
