package ru.antonov.train_ticket_service.ticket_purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Stop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StopRepository extends JpaRepository<Stop, UUID> {

    @Query("""
            SELECT st FROM Stop st
            JOIN FETCH st.cruise c
            JOIN FETCH  c.train t
            JOIN FETCH t.category
            JOIN FETCH st.station
            WHERE st.cruise.id = :cruiseId
            ORDER BY st.plannedArrival
            """)
    List<Stop> findAllByCruiseIdSortByPlannedArrival(UUID cruiseId);


    @Query("""
                SELECT
                    (
                     SELECT st1.stopOrder FROM Stop st1
                     WHERE st1.cruise.id = :cruiseId AND st1.station.id = :fromId
                    )
                    <
                    (
                      SELECT st2.stopOrder FROM Stop st2
                      WHERE st2.cruise.id = :cruiseId AND st2.station.id = :toId
                    )
            """)
    boolean isValidRoute(String cruiseId, UUID fromId, UUID toId);

    @Query("""
            SELECT st from Stop st
            JOIN FETCH st.station
            WHERE st.cruise.id = :cruiseId AND st.station.id = :stationId
            """)
    Optional<Stop> findByCruiseIdAndStationIdWithStation(UUID cruiseId, UUID stationId);

    @Query("""
            SELECT st FROM Stop st
            WHERE st.cruise.id = :cruiseId AND st.stopOrder = :stopOrder
            """)
    Optional<Stop> findByCruiseIdAndStopOrder(UUID cruiseId, Integer stopOrder);

    @Query("""
            SELECT st FROM Stop st
            JOIN FETCH st.station
            WHERE st.cruise.id = :cruiseId AND st.stopOrder = 1
            """)
    Optional<Stop> findFirstStopByCruiseIdWithStation(UUID cruiseId);

    @Query("""
            SELECT st FROM Stop st
            JOIN FETCH st.station
            WHERE st.cruise.id = :cruiseId
            AND st.stopOrder = (SELECT MAX(stopOrder) FROM Stop s WHERE s.cruise.id = :cruiseId)
            """)
    Optional<Stop> findLastStopByCruiseIdWithStation(UUID cruiseId);

    @Query("""
            SELECT st FROM Stop st
            WHERE st.cruise.id = :cruiseId AND st.stopOrder = 1
            """)
    Optional<Stop> findFirstStopByCruiseId(UUID cruiseId);

    @Query("""
            SELECT st FROM Stop st
            WHERE st.cruise.id = :cruiseId
            AND st.stopOrder = (SELECT MAX(stopOrder) FROM Stop s WHERE s.cruise.id = :cruiseId)
            """)
    Optional<Stop> findLastStopByCruiseId(UUID cruiseId);

    @Query("""
            SELECT s FROM Stop s
            JOIN FETCH s.station
            WHERE s.id = :stopId
            """)
    Optional<Stop> findByIdWithStation(UUID stopId);
}
