package ru.antonov.trainticketservice.common.exception;

public class TicketAlreadyReservedException extends BusinessException{
    public TicketAlreadyReservedException(String message, String debugMessage, ErrorCode code){
        super(message, debugMessage, code);
    }
}
