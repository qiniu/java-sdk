package com.qiniu.sms.model;

import java.util.List;


public final class SignatureInfo {
private List<Item> items;
	
	private int total;
	
	private int page;

	private int pageSize;
	
	public class Item{
		private String id;
		private String signature;
		private String source;
		private String auditStatus;
		private String rejectReason;
		private Long createdAt;
		private Long updatedAt;
	}
	
	public List<Item> getItems() {
		return items;
	}

	public static Class<Item> getItemClass(){
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
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}
