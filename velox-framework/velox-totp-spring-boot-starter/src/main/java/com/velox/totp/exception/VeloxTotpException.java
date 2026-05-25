package com.velox.totp.exception;

public class VeloxTotpException extends RuntimeException {

    public VeloxTotpException(String message) {
        super(message);
    }

    public VeloxTotpException(String message, Throwable cause) {
        super(message, cause);
    }
}
