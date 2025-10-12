package com.cff.common.exception;

public class JsonSerializeException extends RuntimeException {

    private static final long serialVersionUID = 7745137766429814328L;

    public JsonSerializeException() {
    }

    public JsonSerializeException(String message) {
        super(message);
    }

    public JsonSerializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonSerializeException(Throwable cause) {
        super(cause);
    }

    public JsonSerializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
