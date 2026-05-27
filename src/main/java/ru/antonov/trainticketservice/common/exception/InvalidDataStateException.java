package ru.antonov.trainticketservice.common.exception;

public class InvalidDataStateException extends InternalServerException {
    public InvalidDataStateException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
