// codebeat:disable[TOO_MANY_IVARS]MergeBackGround
package com.qiniu.rtc.model;

import lombok.Data;

import java.util.List;

@Data
public class RoomResult {

    private List<UserInfo> users;
    private String error;
    private String status;
    private List<String> rooms;
    private boolean end;
    private int offset;
    private String roomId;

    @Data
    public static class UserInfo {
        private String userId;
    }
}
