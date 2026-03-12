package ru.antonov.train_ticket_service.ticket_purchase.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationResponseDto {
    private UUID id;
    private String name;
    private String location;
}
