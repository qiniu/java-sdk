package com.qiniu.sms;

import com.qiniu.sms.model.MessageInfo;
import com.qiniu.util.StringMap;

/**
 * @author hugo
 * @date 2023-06-07 21:37
 */
public class SmsMap {
    private SmsMap() {
    }

    public static StringMap createSingleMessageMap(MessageInfo messageInfo) {
        StringMap bodyMap = new StringMap();
        bodyMap.putNotEmpty("signature_id", messageInfo.getSignatureId());
        bodyMap.put("template_id", messageInfo.getTemplateId());
        bodyMap.put("mobile", messageInfo.getMobile());
        bodyMap.putNotNull("parameters", messageInfo.getParameters());
        bodyMap.putNotEmpty("seq", messageInfo.getSeq());
        return bodyMap;
    }

    public static StringMap createFulltextMessageMap(String[] mobiles, String content) {
        StringMap bodyMap = new StringMap();
        bodyMap.put("mobiles", mobiles);
        bodyMap.put("content", content);
        return bodyMap;
    }

    public static StringMap createFulltextMessageMap(String[] mobiles, String content, String templateType) {
        StringMap bodyMap = new StringMap();
        bodyMap.put("mobiles", mobiles);
        bodyMap.put("content", content);
        bodyMap.put("template_type", templateType);
        return bodyMap;
    }
}
