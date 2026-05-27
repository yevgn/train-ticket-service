package ru.antonov.trainticketservice.cruise.dto;

import lombok.*;
import ru.antonov.trainticketservice.cruise.entity.TrainCategory;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainResponseDto {
    private UUID id;
    private TrainCategory.Category category;
}
