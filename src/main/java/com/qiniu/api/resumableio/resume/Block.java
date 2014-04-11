package com.qiniu.api.resumableio.resume;


public class Block {
	private int idx;
	private String ctx;
	private String key;
	private boolean newAdd;

	public Block(int idx, String ctx){
		this(idx, ctx, false);
	}
	
	public Block(int idx, String ctx, boolean newAdd){
		this.idx = idx;
		this.ctx = ctx;
		this.newAdd = newAdd;
	}
	
	public int getIdx() {
		return idx;
	}

	public String getCtx() {
		return ctx;
	}

	public String getKey() {
		return key;
	}
	
	
	public boolean isNewAdd() {
		return newAdd;
	}

	public void setNewAdd(boolean newAdd) {
		this.newAdd = newAdd;
	}

}
