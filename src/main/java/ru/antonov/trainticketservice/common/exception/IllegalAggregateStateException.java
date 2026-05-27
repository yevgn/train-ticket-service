package ru.antonov.trainticketservice.common.exception;

public class IllegalAggregateStateException extends RuntimeException{
    public IllegalAggregateStateException(String message){
        super(message);
    }
}
