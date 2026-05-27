package ru.antonov.trainticketservice.common.exception;

public class InvalidStopOrderException extends BusinessException {
    public InvalidStopOrderException(String message, String debugMessage, ErrorCode code) {
        super(message, debugMessage, code);
    }
}
