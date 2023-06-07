package com.qiniu.sms.model;

import java.util.Map;

public class MessageInfo {
    private final String signatureId;
    private final String templateId;
    private final String mobile;
    private final Map<String, String> parameters;
    private final String seq;

    /**
     * @param signatureId 签名Id，选填
     * @param templateId  模板Id，必填
     * @param mobile      手机号码，必填
     * @param parameters  参数,选填
     * @param seq         序列号,选填
     */
    public MessageInfo(String signatureId, String templateId, String mobile, Map<String, String> parameters, String seq) {
        this.signatureId = signatureId;
        this.templateId = templateId;
        this.mobile = mobile;
        this.parameters = parameters;
        this.seq = seq;
    }

    public String getSignatureId() {
        return signatureId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getMobile() {
        return mobile;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getSeq() {
        return seq;
    }
}
