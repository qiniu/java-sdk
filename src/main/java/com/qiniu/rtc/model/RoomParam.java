// codebeat:disable[TOO_MANY_IVARS]
package com.qiniu.rtc.model;

import lombok.Data;

@Data
public class RoomParam {
    private String roomName;
    private int maxUsers;
    private int autoCloseTtlS;
    private int noEntreTtlS;
    private boolean openRoom;
}
