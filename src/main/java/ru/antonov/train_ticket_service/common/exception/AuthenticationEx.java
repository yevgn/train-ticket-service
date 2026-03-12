package ru.antonov.train_ticket_service.common.exception;

public class AuthenticationEx extends BusinessException {
    public AuthenticationEx(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
