package ru.antonov.trainticketservice.cruise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.antonov.trainticketservice.cruise.entity.CarriageCategory;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CarriageResponseDto {
    @JsonProperty("carriage_id")
    private UUID carriageId;
    private Integer number;
    private CarriageCategory.Category category;
}
