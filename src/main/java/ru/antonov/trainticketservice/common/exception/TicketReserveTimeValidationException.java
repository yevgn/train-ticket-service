package ru.antonov.trainticketservice.common.exception;

public class TicketReserveTimeValidationException extends BusinessException{
    public TicketReserveTimeValidationException(String message, String debugMessage, ErrorCode code){
        super(message, debugMessage, code);
    }
}
