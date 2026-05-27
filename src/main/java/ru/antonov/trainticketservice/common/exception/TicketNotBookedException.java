package ru.antonov.trainticketservice.common.exception;

public class TicketNotBookedException extends BusinessException{
    public TicketNotBookedException(String message, String debugMessage, ErrorCode code){
        super(message, debugMessage, code);
    }
}
