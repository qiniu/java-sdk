package com.qiniu.sms.model;

import java.util.List;

public class TemplateInfo {

	private int total;

	private int page;

	private int page_size;

	private List<Item> items;

	public class Item {
		private String id;
		private String name;
		private String template;
		private String audit_status;
		private String reject_reason;
		private String type;
		private String signature_id;
		private String signature_text;
		private int created_at;
		private int updated_at;

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
			return audit_status;
		}

		public void setAuditStatus(String auditStatus) {
			this.audit_status = auditStatus;
		}

		public String getRejectReason() {
			return reject_reason;
		}

		public void setRejectReason(String rejectReason) {
			this.reject_reason = rejectReason;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getSignatureId() {
			return signature_id;
		}

		public void setSignatureId(String signatureId) {
			this.signature_id = signatureId;
		}

		public String getSignatureText() {
			return signature_text;
		}

		public void setSignatureText(String signatureText) {
			this.signature_text = signatureText;
		}

		public int getCreatedAt() {
			return created_at;
		}

		public void setCreatedAt(int createdAt) {
			this.created_at = createdAt;
		}

		public int getUpdatedAt() {
			return updated_at;
		}

		public void setUpdatedAt(int updated_at) {
			this.updated_at = updated_at;
		}

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
		return page_size;
	}

	public void setPageSize(int pageSize) {
		this.page_size = pageSize;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

}