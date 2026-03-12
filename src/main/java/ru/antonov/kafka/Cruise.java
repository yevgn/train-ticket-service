package ru.antonov.kafka;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cruise {
    private UUID id;
    private String number;
    private String startLocation;
    private String startStation;
    private String endLocation;
    private String endStation;
    private String status;
}
