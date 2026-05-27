package ru.antonov.trainticketservice.ticket.command.command;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TicketBookCommand implements Command{
    private UUID aggregateId;
    private UUID paymentId;
}
