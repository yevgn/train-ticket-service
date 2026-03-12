package ru.antonov.train_ticket_service.ticket_purchase.dto;

import lombok.*;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Cruise;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CruiseShortResponseDto {
    private UUID id;
    private String number;
    private StopResponseDto from;
    private StopResponseDto to;
    private Cruise.Status status;
    private TrainResponseDto train;
}
