package ru.antonov.trainticketservice.ticket.outbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.antonov.trainticketservice.ticket.outbox.entity.OutboxEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEntry, UUID> {
    @Query(value = """
            SELECT *
            FROM outbox o
            WHERE o.status = 'PENDING'
              AND (
                    o.last_attempt_at IS NULL OR
                    o.last_attempt_at <= CURRENT_TIMESTAMP
                        - (o.retry_count * :retryDelaySec) * INTERVAL '1 second'
                  )
            ORDER BY o.created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEntry> fetchPendingEntries(
            int retryDelaySec,
            int batchSize
    );

    @Query(value = """
            UPDATE outbox SET last_attempt_at = :lastAttemptAt, retry_count = retry_count + 1
            WHERE id = :entryId
            """, nativeQuery = true)
    void updateLastAttemptAndRetryCount(UUID entryId, LocalDateTime lastAttemptAt);


    @Query(value = """
            UPDATE outbox SET status = 'SENT', processed_at = :lastAttemptAt, lastAttemptAt = :lastAttemptAt
            WHERE id = :entryId
            """, nativeQuery = true)
    void markProcessed(UUID entryId, LocalDateTime lastAttemptAt);

    @Query(value = """
            UPDATE outbox SET status = 'DEAD', lastAttemptAt = :lastAttemptAt
            WHERE id = :entryId
            """, nativeQuery = true)
    void markDead(UUID entryId, LocalDateTime lastAttemptAt);

}

