package ru.antonov.trainticketservice.ticket.eventstore.eventdata;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketReservedEventData implements EventData {
    private UUID aggregateId;

    private UUID ownerId;
    private String ownerSurname;
    private String ownerName;
    private String ownerPatronymic;
    private String ownerEmail;

    private UUID cruiseId;
    private String cruiseNumber;

    private UUID carriageId;
    private int carriageNumber;
    private String carriageCategory;

    private UUID seatId;
    private String seatNumber;
    private String seatCategory;

    private UUID fromStopId;
    private String fromLocation;
    private String fromStation;
    private LocalDateTime departureTime;

    private UUID toStopId;
    private String toLocation;
    private String toStation;
    private LocalDateTime arrivalTime;

    private LocalDateTime reservedAt;

    private UUID paymentIdempotencyKey;

    private Float fare;
}
