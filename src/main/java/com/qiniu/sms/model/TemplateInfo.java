package com.qiniu.sms.model;

import java.util.List;


public class TemplateInfo {

    private int total;

    private int page;

    private int pageSize;

    private List<Item> items;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item {
        private String id;
        private String name;
        private String template;
        private String auditStatus;
        private String rejectReason;
        private String type;
        private String signatureId;
        private String signatureText;
        private int createdAt;
        private int updatedAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getAuditStatus() {
            return auditStatus;
        }

        public void setAuditStatus(String auditStatus) {
            this.auditStatus = auditStatus;
        }

        public String getRejectReason() {
            return rejectReason;
        }

        public void setRejectReason(String rejectReason) {
            this.rejectReason = rejectReason;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSignatureId() {
            return signatureId;
        }

        public void setSignatureId(String signatureId) {
            this.signatureId = signatureId;
        }

        public String getSignatureText() {
            return signatureText;
        }

        public void setSignatureText(String signatureText) {
            this.signatureText = signatureText;
        }

        public int getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(int createdAt) {
            this.createdAt = createdAt;
        }

        public int getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(int updated_at) {
            this.updatedAt = updated_at;
        }

    }

}
