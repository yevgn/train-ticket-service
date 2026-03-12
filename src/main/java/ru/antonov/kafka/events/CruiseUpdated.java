package ru.antonov.kafka.events;

import lombok.*;
import ru.antonov.kafka.Cruise;
import ru.antonov.kafka.User;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CruiseUpdated {
    @ToString.Exclude
    private List<User> targetUsers;
    private Cruise cruise;
    private String updatedStopStationName;
    private String updatedStopStationLocation;
    private LocalDateTime oldPlannedArrival;
    private LocalDateTime oldPlannedDeparture;
    private LocalDateTime newPlannedArrival;
    private LocalDateTime newPlannedDeparture;
}
