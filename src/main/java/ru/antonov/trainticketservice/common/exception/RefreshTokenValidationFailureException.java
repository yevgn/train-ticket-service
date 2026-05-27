package ru.antonov.trainticketservice.common.exception;

public class RefreshTokenValidationFailureException extends BusinessException {
    public RefreshTokenValidationFailureException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
