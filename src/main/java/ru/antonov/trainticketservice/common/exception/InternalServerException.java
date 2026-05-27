package ru.antonov.trainticketservice.common.exception;

public class InternalServerException extends CustomException {
    public InternalServerException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
