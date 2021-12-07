package com.qiniu.rtc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageParam {
    int offset;
    int limit;
}
