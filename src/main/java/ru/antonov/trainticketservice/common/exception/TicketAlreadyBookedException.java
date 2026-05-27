package ru.antonov.trainticketservice.common.exception;

public class TicketAlreadyBookedException extends BusinessException {
    public TicketAlreadyBookedException(String message, String debugMessage, ErrorCode code) {
        super(message, debugMessage, code);
    }
}
