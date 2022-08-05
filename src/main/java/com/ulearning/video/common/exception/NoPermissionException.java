package com.ulearning.video.common.exception;

/**
 * 类说明: 缺少权限异常，当操作不符合权限规则时抛出。
 */
public class NoPermissionException extends RuntimeException {
    /* serialVersionUID: serialVersionUID */
    private static final long serialVersionUID = 1L;

    public NoPermissionException() {
        super();
    }

    public NoPermissionException(String s) {
        super(s);
    }
	
    public NoPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public NoPermissionException(Throwable cause) {
        super(cause);
    }
}
