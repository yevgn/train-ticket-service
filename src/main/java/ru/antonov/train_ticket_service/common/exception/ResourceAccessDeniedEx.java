package ru.antonov.train_ticket_service.common.exception;

public class ResourceAccessDeniedEx extends BusinessException {
    public ResourceAccessDeniedEx(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
