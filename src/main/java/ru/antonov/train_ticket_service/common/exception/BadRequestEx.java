package ru.antonov.train_ticket_service.common.exception;

public class BadRequestEx extends BusinessException {
  public BadRequestEx(String message, String debugMessage, ErrorCode errorCode) {
    super(message, debugMessage, errorCode);
  }
}
