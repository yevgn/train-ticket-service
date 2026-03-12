package ru.antonov.train_ticket_service.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    DB_CONSTRAINT_VIOLATION(HttpStatus.CONFLICT),
    AUTH_FAILURE(HttpStatus.UNAUTHORIZED),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND),
    CONFIG_PROPERTIES_NOT_SET(HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_DATA_STATE(HttpStatus.INTERNAL_SERVER_ERROR),
    REFRESH_TOKEN_VALIDATION_FAILURE(HttpStatus.BAD_REQUEST),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    DATA_CONFLICT(HttpStatus.CONFLICT),
    DATA_MISMATCH(HttpStatus.BAD_REQUEST),
    BAD_REQUEST(HttpStatus.BAD_REQUEST)
    ;

    private final HttpStatus status;
}
