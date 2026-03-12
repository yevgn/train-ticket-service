package ru.antonov.train_ticket_service.ticket_purchase.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateTimeRangeValidator.class)
public @interface DateTimeRangeIsCorrect {
    String message() default "Некорректный временной диапазон";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
