package ru.antonov.trainticketservice.ticket.eventstore.eventdata;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketBookedEventData implements EventData {
    private UUID aggregateId;

    private UUID ownerId;
    private String ownerEmail;
    private String ownerFullName;

    private UUID cruiseId;
    private String cruiseNumber;

    private UUID fromStopId;
    private String fromLocation;
    private String toLocation;

    private UUID toStopId;
    private String fromStation;
    private String toStation;

    private LocalDateTime plannedDeparture;

    private Float fare;

    private UUID seatId;
    private String seatNumber;

    private LocalDateTime bookedAt;

    private UUID paymentId;
}