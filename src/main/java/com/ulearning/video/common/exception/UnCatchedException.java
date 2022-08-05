package com.ulearning.video.common.exception;


/**
 * 类说明:无法确知的异常
 */
public class UnCatchedException extends RuntimeException {
    /* serialVersionUID: serialVersionUID */
    private static final long serialVersionUID = 1L;

    public UnCatchedException() {
        super();
    }

    public UnCatchedException(String s) {
        super(s);
    }
	
    public UnCatchedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UnCatchedException(Throwable cause) {
        super(cause);
    }
}
