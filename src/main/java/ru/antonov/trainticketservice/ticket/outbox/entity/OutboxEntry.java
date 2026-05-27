package ru.antonov.trainticketservice.ticket.outbox.entity;


import jakarta.persistence.*;
import lombok.*;
import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;

import java.time.LocalDateTime;
import java.util.UUID;

// todo ПОВЕСИТЬ ИНДЕКСЫ В БД

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "outbox")
public class OutboxEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @Column(name = "aggregate_type")
    private String aggregateType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private Event.EventType eventType;

    @Column(columnDefinition = "text")
    private String payload;

    @Column(columnDefinition = "text")
    private String headers;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "error_message")
    private String errorMessage;

    public enum Status {
        PENDING,
        SENT,
        DEAD
    }
}
