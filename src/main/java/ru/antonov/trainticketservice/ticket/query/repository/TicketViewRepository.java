package ru.antonov.trainticketservice.ticket.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.antonov.trainticketservice.ticket.query.entity.TicketView;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketViewRepository extends JpaRepository<TicketView, UUID> {

    List<TicketView> findAllByOwnerId(UUID ownerId);

    @Query("""
                SELECT t FROM TicketView t
                WHERE t.cruiseId = :cruiseId
                AND (t.fromStopId = :stopId OR t.toStopId = :stopId)
                AND t.status IN ('BOOKED', 'CANCEL_PENDING', 'FAILED_TO_CANCEL')
                AND (
                    t.departureTime > CURRENT_TIMESTAMP
                    OR t.arrivalTime > CURRENT_TIMESTAMP
                )
            """)
    List<TicketView> findAllByCruiseIdAndStopId(UUID cruiseId, UUID stopId);
}
