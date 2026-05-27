package ru.antonov.trainticketservice.common.exception;

public class AuthenticationException extends BusinessException {
    public AuthenticationException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
