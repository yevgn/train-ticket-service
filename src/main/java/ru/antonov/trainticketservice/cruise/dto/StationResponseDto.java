package ru.antonov.trainticketservice.cruise.dto;

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
