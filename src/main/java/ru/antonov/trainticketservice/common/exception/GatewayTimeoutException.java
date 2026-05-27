package ru.antonov.trainticketservice.common.exception;

public class GatewayTimeoutException extends BusinessException {
    public GatewayTimeoutException(String message, String debugMessage, ErrorCode code) {
        super(message, debugMessage, code);
    }
}
