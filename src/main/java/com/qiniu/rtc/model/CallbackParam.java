package com.qiniu.rtc.model;

import lombok.Data;

@Data
public class CallbackParam {
    private String eventCbUrl;
    private String eventCbSecret;
    private int eventCbVersion;
}
