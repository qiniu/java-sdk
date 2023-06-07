package com.qiniu.sms.model;

import java.util.Map;

public class MessageInfo {
    private String signatureId;
    private String templateId;
    private String mobile;
    private Map<String, String> parameters;
    private String seq;

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
