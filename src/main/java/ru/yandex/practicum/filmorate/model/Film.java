package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.DateNotBefore;

import java.time.LocalDate;

@Data
@EqualsAndHashCode
@Builder
public class Film {
    private long id;
    @NotBlank
    private String name;
    @Size(message = "Максимальная длина описания - 200 символов", max = 200)
    private String description;
    @DateNotBefore(value = "1895-12-28", message = "Дата должна быть после 28.12.1895")
    private LocalDate releaseDate;
    @Positive
    private int duration;
}
