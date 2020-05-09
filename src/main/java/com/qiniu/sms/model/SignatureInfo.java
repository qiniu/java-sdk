package com.qiniu.sms.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class SignatureInfo {
    private List<Item> items;

    private int total;

    private int page;

    @SerializedName("page_size")
    private int pageSize;

    public static Class<Item> getItemClass() {
        return Item.class;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

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

    public class Item {
        private String id;
        private String signature;
        private String source;

        @SerializedName("audit_status")
        private String auditStatus;

        @SerializedName("reject_reason")
        private String rejectReason;

        @SerializedName("created_at")
        private Long createdAt;

        @SerializedName("updated_at")
        private Long updatedAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
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

        public Long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Long createdAt) {
            this.createdAt = createdAt;
        }

        public Long getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Long updatedAt) {
            this.updatedAt = updatedAt;
        }

    }

}
