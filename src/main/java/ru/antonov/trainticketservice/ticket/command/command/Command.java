package ru.antonov.trainticketservice.ticket.command.command;

import java.util.UUID;

public interface Command {
    UUID getAggregateId();
}
