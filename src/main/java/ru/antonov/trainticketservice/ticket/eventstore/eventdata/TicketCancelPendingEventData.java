package ru.antonov.trainticketservice.ticket.eventstore.eventdata;

import lombok.Builder;
import lombok.Data;


import java.util.UUID;

@Data
@Builder
public class TicketCancelPendingEventData implements EventData {
    private UUID aggregateId;

    private UUID paymentId;
    private UUID ownerId;
    private Float amount;
    private UUID seatId;
    private UUID stopFromId;
    private UUID stopToId;
    private UUID cruiseId;
    private UUID idempotencyKey;
}
