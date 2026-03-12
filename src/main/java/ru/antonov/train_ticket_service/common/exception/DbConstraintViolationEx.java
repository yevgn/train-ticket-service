package ru.antonov.train_ticket_service.common.exception;

public class DbConstraintViolationEx extends BusinessException {
    public DbConstraintViolationEx(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
