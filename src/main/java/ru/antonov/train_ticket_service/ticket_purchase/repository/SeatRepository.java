package ru.antonov.train_ticket_service.ticket_purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.antonov.train_ticket_service.ticket_purchase.dto.SeatResponseDto;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Seat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    @Query("""
                    SELECT new ru.antonov.train_ticket_service.ticket_purchase.dto.SeatResponseDto(
                           s.id,
                           s.number,
                           sc.category,
                           cf.baseFare,
                           new ru.antonov.train_ticket_service.ticket_purchase.dto.CarriageResponseDto(
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
                    AND NOT EXISTS (
                                    SELECT 1 FROM Ticket tk  WHERE tk.seat = s AND tk.cruise = cr AND tk.status = 'PURCHASED'
                                    AND tk.to.stopOrder > :fromStopOrder
                                    AND tk.from.stopOrder < :toStopOrder
                                )
                    ORDER BY c.number, s.number
            """)
    List<SeatResponseDto> findAvailableSeatsByTargetStopsSortByNumber(
           UUID cruiseId,  int fromStopOrder, int toStopOrder
    );

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
