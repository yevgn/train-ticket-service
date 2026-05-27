package ru.antonov.trainticketservice.cruise.dto;

import java.time.LocalDateTime;

public interface HasDateTimeRange {
    LocalDateTime getStart();
    LocalDateTime getEnd();
}
