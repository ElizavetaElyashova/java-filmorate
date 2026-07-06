package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findById(Long id);

    Film create(@RequestBody @Valid Film film);

    Film update(@RequestBody @Valid Film newFilm);
}
