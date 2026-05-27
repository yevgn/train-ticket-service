package ru.antonov.trainticketservice.common.exception;

public class TicketAlreadyCancellingException extends BusinessException{
    public TicketAlreadyCancellingException(String message, String debugMessage, ErrorCode code){
        super(message, debugMessage, code);
    }
}
