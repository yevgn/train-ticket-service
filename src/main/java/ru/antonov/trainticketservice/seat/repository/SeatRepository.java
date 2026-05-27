package ru.antonov.trainticketservice.seat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.antonov.trainticketservice.seat.dto.SeatResponseDto;
import ru.antonov.trainticketservice.seat.entity.Seat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    @Query("""
                    SELECT new ru.antonov.trainticketservice.seat.dto.SeatResponseDto(
                           s.id,
                           s.number,
                           sc.category,
                           cf.baseFare,
                           new ru.antonov.trainticketservice.cruise.dto.CarriageResponseDto(
                               c.id,
                               c.number,
                               cc.category
                           )
                    )
                    FROM Cruise cr
            
                    JOIN cr.train tr
            
                    JOIN tr.carriages c
                    JOIN c.category cc
            
                    JOIN c.seats s
                    JOIN s.category sc
            
                    JOIN CruiseFare cf ON cf.cruise = cr AND cf.seatCategory = sc AND cf.carriageCategory = cc
            
                    WHERE cr.id = :cruiseId

                    ORDER BY c.number, s.number
            """)
    List<SeatResponseDto> findAllByCruiseId(UUID cruiseId);


    @Query("""
            SELECT s FROM Seat s
            
            JOIN FETCH s.category
            
            JOIN FETCH s.carriage c
            JOIN FETCH c.category
            
            JOIN FETCH c.train t
            JOIN FETCH t.category
            
            WHERE s.id = :seatId
            """)
    Optional<Seat> findByIdWithCategoryAndCarriage(UUID seatId);
}
