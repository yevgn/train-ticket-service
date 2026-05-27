package ru.antonov.trainticketservice.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BusinessException extends CustomException {
    public BusinessException(String message, String debugMessage, ErrorCode errorCode) {
        super(message, debugMessage, errorCode);
    }
}
