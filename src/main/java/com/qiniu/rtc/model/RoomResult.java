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

    @Data
    public static class UserInfo {
        private String userId;
    }
}
