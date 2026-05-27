package ru.antonov.trainticketservice.cruise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.antonov.trainticketservice.cruise.entity.Stop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StopRepository extends JpaRepository<Stop, UUID> {

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
            SELECT s FROM Stop s
            JOIN FETCH s.station
            WHERE s.id = :stopId
            """)
    Optional<Stop> findByIdWithStation(UUID stopId);
}
