package com.cff.cache.queue.exception;

public class CacheQueueException extends RuntimeException {

    private static final long serialVersionUID = -2771337737632095851L;

    public CacheQueueException() {
        super();
    }

    public CacheQueueException(String message) {
        super(message);
    }

    public CacheQueueException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheQueueException(Throwable cause) {
        super(cause);
    }

    protected CacheQueueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
