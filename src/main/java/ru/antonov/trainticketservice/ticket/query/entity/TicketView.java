package ru.antonov.trainticketservice.ticket.query.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "ticket_view")
public class TicketView {
    @Id
    private UUID ticketId;

    private UUID ownerId;
    private String ownerSurname;
    private String ownerName;
    private String ownerPatronymic;
    private String ownerEmail;

    private UUID cruiseId;
    private String cruiseNumber;

    private UUID fromStopId;
    private String fromLocation;
    private String fromStation;
    private LocalDateTime departureTime;

    private UUID toStopId;
    private String toLocation;
    private String toStation;
    private LocalDateTime arrivalTime;

    private UUID seatId;
    private String seatNumber;
    private String seatCategory;

    private UUID carriageId;
    private Integer carriageNumber;
    private String carriageCategory;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Float fare;
    private UUID paymentId;
    private UUID refundId;

    private LocalDateTime bookedAt;
    private LocalDateTime failedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime lastUpdatedAt;

    public enum Status{
        RESERVED,
        BOOKED,
        CANCEL_PENDING,
        CANCELLED,
        FAILED_TO_BOOK,
        FAILED_TO_CANCEL
    }
}
