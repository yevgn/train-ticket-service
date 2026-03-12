package ru.antonov.train_ticket_service.common.exception;

public class EntityNotFoundEx extends BusinessException {
    public EntityNotFoundEx(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
