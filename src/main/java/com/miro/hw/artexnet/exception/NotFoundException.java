package com.miro.hw.artexnet.exception;

import com.miro.hw.artexnet.common.ErrorCode;

public class NotFoundException extends RuntimeException {
    protected ErrorCode errorCode;

    public NotFoundException() {
        super();
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}
