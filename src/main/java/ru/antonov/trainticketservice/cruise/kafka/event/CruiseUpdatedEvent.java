package ru.antonov.trainticketservice.cruise.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CruiseUpdatedEvent {
    private UUID cruiseId;
    private String cruiseNumber;

    private UUID updatedStopId;
    private String updatedStopLocation;
    private String updatedStopName;

    private LocalDateTime oldDeparture;
    private LocalDateTime oldArrival;
    private LocalDateTime newDeparture;
    private LocalDateTime newArrival;

    private List<User> targetUsers;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class User{
        private UUID id;
        private String email;
        private String fullName;
    }
}
