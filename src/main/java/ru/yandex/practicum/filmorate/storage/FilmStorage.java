package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findById(Long id);

    List<Film> findCommonFilms(Long userId, Long friendId);

    Film create(@RequestBody @Valid Film film);

    Film update(@RequestBody @Valid Film newFilm);

    void remove(Long id);

}
