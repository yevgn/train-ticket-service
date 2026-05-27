package ru.antonov.trainticketservice.common.exception;

public class TicketAlreadyCancelledException extends BusinessException{
    public TicketAlreadyCancelledException(String message, String debugMessage, ErrorCode code){
        super(message, debugMessage, code);
    }
}
