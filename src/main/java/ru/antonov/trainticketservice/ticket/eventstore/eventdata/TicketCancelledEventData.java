package ru.antonov.trainticketservice.ticket.eventstore.eventdata;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketCancelledEventData implements EventData {
    private UUID aggregateId;

    private UUID ownerId;
    private String ownerEmail;
    private String ownerFullName;

    private UUID cruiseId;
    private String cruiseNumber;

    private UUID fromStopId;
    private String fromLocation;
    private String fromStation;

    private UUID toStopId;
    private String toLocation;
    private String toStation;

    private UUID seatId;
    private String seatNumber;

    private LocalDateTime plannedDeparture;

    private LocalDateTime cancelledAt;

    private Float amount;

    private UUID refundId;
}
