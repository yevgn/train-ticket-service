package ru.antonov.trainticketservice.common.exception;

public class ResourceAccessDeniedException extends BusinessException {
    public ResourceAccessDeniedException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
