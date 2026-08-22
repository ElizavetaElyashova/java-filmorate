package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {
    private long reviewId;
    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;
    @NotBlank
    private String content;
    @NotNull
    private Boolean isPositive;
    private Integer useful;
}
