package ru.antonov.trainticketservice.common.exception;

public class TicketOfOtherUserException extends BusinessException{
    public TicketOfOtherUserException(String message, String debugMessage, ErrorCode code){
        super(message, debugMessage, code);
    }
}
