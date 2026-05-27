package ru.antonov.trainticketservice.ticket.eventstore.eventdata;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketFailedToCancelEventData implements EventData {
    private UUID aggregateId;

    private UUID ownerId;
    private String ownerEmail;
    private String ownerFullName;

    private UUID cruiseId;
    private String cruiseNumber;

    private UUID stopFromId;
    private String fromLocation;
    private String fromStation;

    private UUID stopToId;
    private String toLocation;
    private String toStation;

    private LocalDateTime plannedDeparture;

    private UUID seatId;
    private String seatNumber;

    private LocalDateTime failedAt;
    private String errorMessage;

    private Float amount;

    private UUID refundId;
}
