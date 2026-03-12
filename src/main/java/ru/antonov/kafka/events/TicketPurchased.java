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
public class TicketPurchased  {
    private UUID ticketId;
    private User user;
    private Cruise cruise;
    private String departureStationLocation;
    private String arrivalStationLocation;
    private String departureStationName;
    private String arrivalStationName;
    private LocalDateTime purchasedAt;
    private Float fare;
}