package ru.antonov.train_ticket_service.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CustomException extends RuntimeException {
    private String debugMessage;
    private ErrorCode errorCode;

    public CustomException(String message, String debugMessage, ErrorCode errorCode) {
        super(message);
        this.debugMessage = debugMessage;
        this.errorCode = errorCode;
    }
}
