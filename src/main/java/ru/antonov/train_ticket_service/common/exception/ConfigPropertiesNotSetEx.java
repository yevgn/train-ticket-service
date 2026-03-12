package ru.antonov.train_ticket_service.common.exception;

public class ConfigPropertiesNotSetEx extends InternalServerException {
    public ConfigPropertiesNotSetEx(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
