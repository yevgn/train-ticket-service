package ru.antonov.trainticketservice.ticket.command.command;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TicketReserveCommand implements Command{
    private UUID aggregateId;
    private UUID seatId;
    private UUID stopFromId;
    private UUID stopToId;
    private UUID userId;
}
