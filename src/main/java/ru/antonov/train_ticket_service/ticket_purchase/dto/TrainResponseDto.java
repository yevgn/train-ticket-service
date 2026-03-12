package ru.antonov.train_ticket_service.ticket_purchase.dto;

import lombok.*;
import ru.antonov.train_ticket_service.ticket_purchase.entity.TrainCategory;

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
