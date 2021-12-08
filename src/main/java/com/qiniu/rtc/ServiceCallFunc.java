package com.qiniu.rtc;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

public interface ServiceCallFunc {
    Response call() throws QiniuException;
}
