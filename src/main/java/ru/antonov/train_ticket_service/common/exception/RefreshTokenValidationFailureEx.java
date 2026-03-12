package ru.antonov.train_ticket_service.common.exception;

public class RefreshTokenValidationFailureEx extends BusinessException {
    public RefreshTokenValidationFailureEx(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
