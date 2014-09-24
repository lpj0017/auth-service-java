package com.rcloud.auth;

public class RongAuthException extends Exception {
	
	private static final long serialVersionUID = -8572656518755621317L;
	
	private int errorCode;
	
	public RongAuthException(int errorCode, String msg, Throwable cause){
		super(msg, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}
