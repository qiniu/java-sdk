package com.qiniu.sms.model;

import java.util.List;

public final class SignatureInfo {
	private List<Item> items;

	private int total;

	private int page;

	private int page_size;

	public class Item {
		private String id;
		private String signature;
		private String source;
		private String audit_status;
		private String reject_reason;
		private Long created_at;
		private Long updated_at;

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

		public Long getCreatedAt() {
			return created_at;
		}

		public void setCreatedAt(Long createdAt) {
			this.created_at = createdAt;
		}

		public Long getUpdatedAt() {
			return updated_at;
		}

		public void setUpdatedAt(Long updatedAt) {
			this.updated_at = updatedAt;
		}

	}

	public List<Item> getItems() {
		return items;
	}

	public static Class<Item> getItemClass() {
		return Item.class;
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
		return page_size;
	}

	public void setPageSize(int pageSize) {
		this.page_size = pageSize;
	}

}