package com.qiniu.api.resumable.io;

/*
 * 类 PutExtra 指定在断点续传操作中额外的参数
 */
public class PutExtra {
	/**
	 * 回调参数, 通常为可选。 当生成上传凭证 uptoken @see com.qiniu.api.rs.PutPolicy 时如果指定了
	 * callbackurl 参数， 该字段则必须填写表示当文件从客户端成功上传至七牛云存储服务器之后，云存储服务器向业务服务器（由
	 * callbackurl 指定）以 post 请求发送该字段
	 */
	public String callbackParams;

	/** 文件要上传到的目标空间名称，目前为必选 */
	public String bucketName;

	/**
	 * 为执行远程回调指定 Content-Type，比如可以是：application/x-www-form-urlencoded, 可选字段
	 */
	public String customMeta;

	/**
	 * 表明文件的 MIME 类型，缺省情况下为 application/octet-stream，可选字段
	 */
	public String mimeType;

	/**
	 * 上传块的大小，如果不指定默认大小为 256KB，可选字段
	 */
	public long chunkSize;

	/**
	 * 上传失败后重试次数，如果不指定默认重试 3 次， 可选字段
	 */
	public int retryTimes;

	/**
	 * 上传文件的校验和，由云存储服务端返回，做断点续传用。 可选字段
	 */
	public String[] checksums;

	/**
	 * 上传文件进度，做断点续传用，如果不指定上传进度则表示文件从头开始传。可选字段
	 */
	public BlockProgress[] progresses;

	/**
	 * 回调接口，当客户端上传完一个 block 之后执行的动作。可选字段
	 */
	public ProgressNotifier progressNotifier;

	/**
	 * 回调接口，当客户端上传完一个 chunk 之后执行的动作。可选字段
	 */
	public BlockProgressNotifier blockProgressNotifier;

	public PutExtra() {
	}

	public PutExtra(String bucketName) {
		this.bucketName = bucketName;
	}
}
