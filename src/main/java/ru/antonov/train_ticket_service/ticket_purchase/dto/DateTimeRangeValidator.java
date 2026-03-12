package ru.antonov.train_ticket_service.ticket_purchase.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateTimeRangeValidator implements ConstraintValidator<DateTimeRangeIsCorrect, HasDateTimeRange> {

    @Override
    public boolean isValid(HasDateTimeRange hasDateTimeRange, ConstraintValidatorContext context) {
        return hasDateTimeRange.getStart().isBefore(hasDateTimeRange.getEnd());
    }
}
