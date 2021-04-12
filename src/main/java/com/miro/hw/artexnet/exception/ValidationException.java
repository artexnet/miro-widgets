package com.miro.hw.artexnet.exception;

import com.miro.hw.artexnet.common.ErrorCode;

public class ValidationException extends RuntimeException {
    protected ErrorCode errorCode;

    public ValidationException() {
        super();
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
