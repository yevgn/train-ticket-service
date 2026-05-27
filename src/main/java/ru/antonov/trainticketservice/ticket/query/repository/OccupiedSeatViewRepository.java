package ru.antonov.trainticketservice.ticket.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.antonov.trainticketservice.ticket.query.entity.OccupiedSeatView;

import java.util.List;
import java.util.UUID;

@Repository
public interface OccupiedSeatViewRepository extends JpaRepository<OccupiedSeatView, UUID> {
    List<OccupiedSeatView> findAllByCruiseId(UUID cruiseId);

    @Query("""
            DELETE FROM OccupiedSeatView v WHERE v.seatId = :seatId and v.cruiseId = :cruiseId
            """)
    void deleteBySeatIdAndCruiseId(UUID seatId, UUID cruiseId);
}
