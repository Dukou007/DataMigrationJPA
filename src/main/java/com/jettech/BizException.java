package com.jettech;

public class BizException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7148399989166521704L;

	public BizException() {
		super();
	}

	public BizException(String message) {
		super(message);
	}

	public BizException(String message, Throwable cause) {
		super(message, cause);
	}

	public BizException(Throwable cause) {
		super(cause);
	}
}
