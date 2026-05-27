package ru.antonov.trainticketservice.ticket.eventstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("""
            SELECT e FROM Event e
            WHERE e.aggregateId = :aggregateId
            ORDER BY e.aggregateVersion
            """)
    List<Event> load(UUID aggregateId);

    @Query("""
            SELECT e FROM Event e ORDER BY e.timestamp ASC
            """)
    List<Event> findAllOrderByTimestamp();
}
