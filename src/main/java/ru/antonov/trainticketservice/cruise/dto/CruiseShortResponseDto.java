package ru.antonov.trainticketservice.cruise.dto;

import lombok.*;
import ru.antonov.trainticketservice.cruise.entity.Cruise;

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
