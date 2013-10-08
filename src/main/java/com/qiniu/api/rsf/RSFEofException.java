package com.qiniu.api.rsf;

public class RSFEofException extends Exception {

	private static final long serialVersionUID = 1L;

	protected RSFEofException() {
		super();
	}

	public RSFEofException(String detailMessage) {
		super(detailMessage);
	}

	public RSFEofException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public RSFEofException(Throwable throwable) {
		super(throwable);
	}
}
