package com.qiniu.sms.model;

import java.util.Map;

public class MessageInfo {
    private final String signatureId;
    private final String templateId;
    private final String mobile;
    private final Map<String, String> parameters;
    private final String seq;

    /**
     * @param signatureId ǩ��Id��ѡ��
     * @param templateId  ģ��Id������
     * @param mobile      �ֻ����룬����
     * @param parameters  ����,ѡ��
     * @param seq         ���к�,ѡ��
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
