package com.ulearning.video.common.exception;

/**
 * 类说明: 数据不一致异常，当数据库数据不符合业务规则时抛出。
 */

public class DataInconsistentException extends RuntimeException {
    /* serialVersionUID: serialVersionUID */
    private static final long serialVersionUID = 1L;

    public DataInconsistentException() {
        super();
    }

    public DataInconsistentException(String s) {
        super(s);
    }
	
    public DataInconsistentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DataInconsistentException(Throwable cause) {
        super(cause);
    }
}
