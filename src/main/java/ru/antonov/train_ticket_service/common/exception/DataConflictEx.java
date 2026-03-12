package ru.antonov.train_ticket_service.common.exception;

public class DataConflictEx extends BusinessException {
    public DataConflictEx(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
