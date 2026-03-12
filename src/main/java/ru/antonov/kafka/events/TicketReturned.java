package ru.antonov.kafka.events;

import lombok.*;

import ru.antonov.kafka.Cruise;
import ru.antonov.kafka.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TicketReturned {
    private UUID ticketId;
    private User user;
    private Cruise cruise;
    private String departureStationLocation;
    private String arrivalStationLocation;
    private String departureStationName;
    private String arrivalStationName;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private LocalDateTime purchasedAt;
    private LocalDateTime returnedAt;
    private Float fare;
}