package ru.antonov.trainticketservice.common.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
public class ApiError {
    private HttpStatus status;
    private String message;
}