package ru.antonov.trainticketservice.common.exception;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
