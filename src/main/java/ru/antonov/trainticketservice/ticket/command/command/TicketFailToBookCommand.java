package ru.antonov.trainticketservice.ticket.command.command;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TicketFailToBookCommand implements Command{
    private UUID aggregateId;
    private UUID paymentId;
    private String errorMessage;
}
