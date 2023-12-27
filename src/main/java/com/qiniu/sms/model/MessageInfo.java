package com.qiniu.sms.model;

import java.util.Map;

public class MessageInfo {
    private String signatureId; // 签名Id，选填
    private String templateId; // 模板Id，必填
    private String mobile; // 手机号码，必填
    private Map<String, String> parameters; // 参数，选填
    private String seq; // 序列号，选填

    public MessageInfo(String templateId, String mobile, Map<String, String> parameters) {
        this.templateId = templateId;
        this.mobile = mobile;
        this.parameters = parameters;
    }

    public void setSignatureId(String signatureId) {
        this.signatureId = signatureId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setSeq(String seq) {
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
