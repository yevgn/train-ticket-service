package ru.antonov.train_ticket_service.common.exception;

public class InvalidDataStateEx extends InternalServerException {
    public InvalidDataStateEx(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
