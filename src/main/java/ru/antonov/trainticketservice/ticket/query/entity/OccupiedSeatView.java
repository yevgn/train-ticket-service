package ru.antonov.trainticketservice.ticket.query.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "occupied_seats_view")
public class OccupiedSeatView {
    @Id
    private UUID id;

    private UUID seatId;
    private String seatNumber;

    private UUID cruiseId;
    private String cruiseNumber;

    private UUID ticketId;
    private String owner;

    private UUID fromId;
    private String fromLocation;
    private Integer fromOrder;

    private UUID toId;
    private String toLocation;
    private Integer toOrder;

}
