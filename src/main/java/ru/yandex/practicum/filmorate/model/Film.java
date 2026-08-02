package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.DateNotBefore;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
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
    private Integer duration;
    private int likes;
    private Set<Long> usersLikedIds;
    private Mpa mpa;
    private List<Genre> genres;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return id == film.id && Objects.equals(name, film.name) && Objects.equals(releaseDate, film.releaseDate) && Objects.equals(duration, film.duration) && Objects.equals(mpa, film.mpa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, releaseDate, duration, mpa);
    }
}
