package ru.antonov.trainticketservice.ticket.command.command;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TicketFailToCancelCommand implements Command{
    private UUID aggregateId;
    private UUID refundId;
    private String errorMessage;
}
