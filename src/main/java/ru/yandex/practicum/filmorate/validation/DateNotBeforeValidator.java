package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class DateNotBeforeValidator implements ConstraintValidator<DateNotBefore, LocalDate> {
    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if (localDate == null) {
            return true;
        }
        try {
            LocalDate targetDate = LocalDate.parse(Film.class.getDeclaredField("releaseDate").getAnnotation(DateNotBefore.class).value());
            return localDate.isAfter(targetDate) || localDate.isEqual(targetDate);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
