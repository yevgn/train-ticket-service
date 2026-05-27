package ru.antonov.trainticketservice.common.exception;

public class ConcurrentAggregateModificationException extends BusinessException{
    public ConcurrentAggregateModificationException(String message, String debugMessage, ErrorCode code){
        super(message, debugMessage, code);
    }
}
