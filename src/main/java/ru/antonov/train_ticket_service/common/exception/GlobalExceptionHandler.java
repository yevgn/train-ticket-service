package ru.antonov.train_ticket_service.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(
            {MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
                    HttpMessageNotReadableException.class, NoResourceFoundException.class, MissingPathVariableException.class,
                    HttpRequestMethodNotSupportedException.class}
    )
    public ResponseEntity<ApiError> handleStandard4xxExceptions(Exception ex) {
        log.warn(ex.getMessage());
        ApiError error = ApiError
                .builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Неуспешная валидация: ", ex);

        BindingResult bindingResult = ex.getBindingResult();

        List<String> errors = new ArrayList<>();

        bindingResult.getFieldErrors().forEach(error ->
                errors.add(error.getDefaultMessage())
        );

        bindingResult.getGlobalErrors().forEach(error ->
                errors.add(error.getDefaultMessage())
        );

        ApiError error = ApiError
                .builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(errors.toString())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleHandlerMethodValidationEx(HandlerMethodValidationException ex) {
        log.warn("Неуспешная валидация: ", ex);
        List<String> errors = new ArrayList<>();

        ex.getAllValidationResults()
                .forEach(paramResult ->
                        paramResult.getResolvableErrors()
                                .forEach(err -> errors.add(err.getDefaultMessage()))
                );

        ApiError error = ApiError
                .builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(errors.toString())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationEx(jakarta.validation.ConstraintViolationException ex) {
        log.warn("Неуспешная валидация: ", ex);
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(errors.toString())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessExceptions(BusinessException ex) {
        log.warn("Business Error: code = {}, message = {}, debugMessage = {}", ex.getErrorCode().getStatus(),
                ex.getMessage(), ex.getDebugMessage());
        ApiError error = ApiError
                .builder()
                .status(ex.getErrorCode().getStatus())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, ex.getErrorCode().getStatus());
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ApiError> handleInternalServerExceptions(InternalServerException ex) {
        log.error("Internal Server Error: code = {}, message = {}, debugMessage = {}", ex.getErrorCode().getStatus(),
                ex.getMessage(), ex.getDebugMessage());
        ApiError error = ApiError
                .builder()
                .status(ex.getErrorCode().getStatus())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, ex.getErrorCode().getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationEx(ConstraintViolationException ex){
        log.warn("Ошибка при добавлении записи в БД:\n{}", ex.getSQLException().getMessage());
        ApiError apiError = ApiError
                .builder()
                .status(HttpStatus.CONFLICT)
                .message("Нарушены ограничения целостности базы данных. Возможно, вы пытаетесь добавить некорректные данные," +
                        " либо они уже существуют/удалить данные некорректным образом")
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }
}