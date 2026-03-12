package ru.antonov.train_ticket_service.ticket_purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    @Query("""
                SELECT COUNT(tk) > 0 FROM Ticket tk
                WHERE tk.seat.id = :seatId AND tk.cruise.id = :cruiseId AND tk.status = 'PURCHASED'
            """)
    boolean isTicketExistsByCruiseIdAndSeatId(UUID cruiseId, UUID seatId);

    @Query("""
            SELECT tk FROM Ticket tk
            
            JOIN FETCH tk.cruise
            
            JOIN FETCH tk.from st1
            JOIN FETCH tk.to st2
            JOIN FETCH st1.station
            JOIN FETCH st2.station
            
            WHERE tk.user.id = :userId
            """)
    List<Ticket> findAllByUserIdWithCruiseAndTargetStops(UUID userId);

    @Query("""
            SELECT tk FROM Ticket tk
            
            JOIN FETCH tk.cruise c
            
            JOIN FETCH c.train t
            JOIN FETCH t.category
            
            JOIN FETCH tk.from fr
            JOIN FETCH fr.station
            
            JOIN FETCH tk.to to
            JOIN FETCH to.station
            
            JOIN FETCH tk.user
            
            WHERE tk.id = :ticketId
            """)
    Optional<Ticket> findByIdWithCruiseAndTargetStopsAndUser(UUID ticketId);
}
