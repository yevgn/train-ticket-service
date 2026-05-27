package ru.antonov.trainticketservice.cruise.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateTimeRangeValidator implements ConstraintValidator<DateTimeRangeIsCorrect, HasDateTimeRange> {

    @Override
    public boolean isValid(HasDateTimeRange hasDateTimeRange, ConstraintValidatorContext context) {
        return hasDateTimeRange.getStart().isBefore(hasDateTimeRange.getEnd());
    }
}
