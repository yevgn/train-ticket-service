package ru.antonov.trainticketservice.cruise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.antonov.trainticketservice.cruise.dto.CruiseShortResponseDto;
import ru.antonov.trainticketservice.cruise.entity.Cruise;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CruiseRepository extends JpaRepository<Cruise, UUID> {

    @Query("""
            SELECT c FROM Cruise c
            JOIN FETCH c.train
            JOIN FETCH c.stops s
            JOIN FETCH s.station
            WHERE c.id = :cruiseId
            """)
    Optional<Cruise> findByIdFetchTrainAndStops(UUID cruiseId);

    @Query("""
                    SELECT new ru.antonov.trainticketservice.cruise.dto.CruiseShortResponseDto(
                        c.id,
                        c.number,
                        new ru.antonov.trainticketservice.cruise.dto.StopResponseDto(
                            st1.id,
                            new ru.antonov.trainticketservice.cruise.dto.StationResponseDto(
                                sta1.id, sta1.name, sta1.location
                            ),
                            st1.stopOrder,
                            st1.plannedArrival,
                            st1.plannedDeparture
                        ),
                        new ru.antonov.trainticketservice.cruise.dto.StopResponseDto(
                            st2.id,
                            new ru.antonov.trainticketservice.cruise.dto.StationResponseDto(
                                sta2.id, sta2.name, sta2.location
                            ),
                            st2.stopOrder,
                            st2.plannedArrival,
                            st2.plannedDeparture
                        ),
                        c.status,
                        new ru.antonov.trainticketservice.cruise.dto.TrainResponseDto(
                            tr.id, ca.category
                        )
                    )
                    FROM Cruise c
            
                    JOIN c.stops st1 ON st1.stopOrder = 1
                    JOIN c.stops st2 ON st2.stopOrder = (SELECT MAX(stopOrder) FROM Stop s WHERE s.cruise = c)
            
                    JOIN c.train tr
                    JOIN tr.category ca
            
                    JOIN st1.station sta1
                    JOIN st2.station sta2
            """)
    List<CruiseShortResponseDto> findAllToDto();

    @Query("""
                    SELECT new ru.antonov.trainticketservice.cruise.dto.CruiseShortResponseDto(
                        c.id,
                        c.number,
                        new ru.antonov.trainticketservice.cruise.dto.StopResponseDto(
                            st1.id,
                            new ru.antonov.trainticketservice.cruise.dto.StationResponseDto(
                                sta1.id, sta1.name, sta1.location
                            ),
                            st1.stopOrder,
                            st1.plannedArrival,
                            st1.plannedDeparture
                        ),
                        new ru.antonov.trainticketservice.cruise.dto.StopResponseDto(
                            st2.id,
                            new ru.antonov.trainticketservice.cruise.dto.StationResponseDto(
                                sta2.id, sta2.name, sta2.location
                            ),
                            st2.stopOrder,
                            st2.plannedArrival,
                            st2.plannedDeparture
                        ),
                        c.status,
                        new ru.antonov.trainticketservice.cruise.dto.TrainResponseDto(
                            tr.id, ca.category
                        )
                    )
                    FROM Cruise c
            
                    JOIN c.stops st1 ON st1.stopOrder = 1
                    JOIN c.stops st2 ON st2.stopOrder = (SELECT MAX(stopOrder) FROM Stop s WHERE s.cruise = c)
            
                    JOIN c.train tr
                    JOIN tr.category ca
            
                    JOIN st1.station sta1
                    JOIN st2.station sta2
            
                    JOIN c.stops st11
                    JOIN c.stops st22
            
                    JOIN st11.station sta11 ON sta11.id = :stationFromId
                    JOIN st22.station sta22 ON sta22.id = :stationToId
            
                    WHERE (st11.plannedArrival >= :fromDate OR st11.plannedDeparture >= :fromDate)
                    AND
                    (st11.plannedArrival <= :toDate OR st22.plannedDeparture <= :toDate)
                    AND
                    st11.stopOrder < st22.stopOrder
            """)
    List<CruiseShortResponseDto> findAllFilterByTargetStationsAndDateRange(
            LocalDateTime fromDate, LocalDateTime toDate, UUID stationFromId, UUID stationToId
    );

    @Query("""
            SELECT c FROM Cruise c
            JOIN FETCH c.train
            WHERE c.id = :cruiseId
            """)
    Optional<Cruise> findByIdWithTrain(UUID cruiseId);
}
