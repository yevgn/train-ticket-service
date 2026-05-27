package ru.antonov.trainticketservice.common.exception;

public class DatabaseException extends BusinessException {
    public DatabaseException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
