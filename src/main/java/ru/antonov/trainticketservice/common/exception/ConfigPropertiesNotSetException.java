package ru.antonov.trainticketservice.common.exception;

public class ConfigPropertiesNotSetException extends InternalServerException {
    public ConfigPropertiesNotSetException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
