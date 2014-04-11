package com.qiniu.api.resumableio.resume;

public interface Resumable {
	
	/**
	 * 总块数
	 * @return
	 */
	int blockCount();
	
	/**
	 * 文件标识
	 * @return
	 */
	String getKey();
	
	/**
	 * 整个资源是否已成功上传
	 * @return
	 */
	boolean isDone();
	
	/**
	 * 检查一个块是否已成功上传
	 * @param idx
	 * @return
	 */
	boolean isBlockDone(int idx);
	
	/**
	 * 已完成的块数
	 * @return
	 */
	int doneCount();
	
	/**
	 * 获取指定块的控制信息ctx内容
	 * @param idx
	 * @return
	 */
	String getCtx(int idx);
	
	Block getBlock(int idx);
	
	void set(Block block);
	
	void drop(Block block);
	
	/**
	 * 加载已保存的上传信息
	 */
	void load() throws Exception;
	
	/**
	 * 保存上传块信息
	 */
	void save() throws Exception;
	
	/**
	 * 上传成功后方可调用此方法
	 * 清除上传信息
	 */
	void clean() throws Exception;
	
}
