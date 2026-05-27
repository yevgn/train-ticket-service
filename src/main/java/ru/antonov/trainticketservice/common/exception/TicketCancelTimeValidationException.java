package ru.antonov.trainticketservice.common.exception;

public class TicketCancelTimeValidationException extends BusinessException {
    public TicketCancelTimeValidationException(String message, String debugMessage, ErrorCode code)
    {
        super(message, debugMessage, code);
    }
}
