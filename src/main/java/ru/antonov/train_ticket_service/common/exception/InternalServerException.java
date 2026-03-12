package ru.antonov.train_ticket_service.common.exception;

public class InternalServerException extends CustomException {
    public InternalServerException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
