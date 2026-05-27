package ru.antonov.trainticketservice.common.exception;

public class DataMismatchException extends BusinessException {
    public DataMismatchException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
