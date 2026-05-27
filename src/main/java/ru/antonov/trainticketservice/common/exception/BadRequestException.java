package ru.antonov.trainticketservice.common.exception;

public class BadRequestException extends BusinessException {
  public BadRequestException(String message, String debugMessage, ErrorCode errorCode) {
    super(message, debugMessage, errorCode);
  }
}
