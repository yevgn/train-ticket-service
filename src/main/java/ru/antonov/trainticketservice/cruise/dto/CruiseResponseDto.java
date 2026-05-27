package ru.antonov.trainticketservice.cruise.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.antonov.trainticketservice.cruise.entity.Cruise;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CruiseResponseDto {
    private UUID id;
    private String number;
    private List<StopResponseDto> stops;
    private Cruise.Status status;
    private TrainResponseDto train;
}
