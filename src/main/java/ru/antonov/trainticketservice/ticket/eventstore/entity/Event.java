package ru.antonov.trainticketservice.ticket.eventstore.entity;


//todo повесить индексы в БД

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сохраняемая запись Event Store для событий агрегата билета.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "event_store")
public class Event{
    @Column(name = "event_id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @Column(name = "aggregate_type")
    private String aggregateType;

    @Column(name = "aggregate_version")
    private long aggregateVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;

    @Transient
    private Object eventData;

    @Column(columnDefinition = "text")
    private String payload;

    private LocalDateTime timestamp;

    @Column(columnDefinition = "text")
    private String metadata;

    public enum EventType{
        TICKET_RESERVED,
        TICKET_BOOKED,
        TICKET_CANCELLED,
        TICKET_FAILED_TO_BOOK,
        TICKET_FAILED_TO_CANCEL,
        TICKET_CANCEL_PENDING
    }
}
