package com.ulearning.video.common.exception;

/**
 * 类说明: 自定义异常。
 */
public class CustomizeException extends RuntimeException {
    /* serialVersionUID: serialVersionUID */
    private static final long serialVersionUID = 1L;

    private int statusCode;
    private int resultCode;
    
    public CustomizeException() {
        super();
    }

    public CustomizeException(String s) {
        super(s);
    }
    
    public CustomizeException(String s, int statusCode, int resultCode) {
        super(s);
        this.statusCode = statusCode;
        this.resultCode = resultCode;
    }
	
    public CustomizeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CustomizeException(Throwable cause) {
        super(cause);
    }

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
}
