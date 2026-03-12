package ru.antonov.train_ticket_service.ticket_purchase.dto;

import java.time.LocalDateTime;

public interface HasDateTimeRange {
    LocalDateTime getStart();
    LocalDateTime getEnd();
}
