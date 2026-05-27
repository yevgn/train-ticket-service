package ru.antonov.trainticketservice.common.exception;

public class DbConstraintViolationException extends BusinessException {
    public DbConstraintViolationException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
