package ru.antonov.train_ticket_service.common.exception;

public class DataMismatchEx extends BusinessException {
    public DataMismatchEx(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
