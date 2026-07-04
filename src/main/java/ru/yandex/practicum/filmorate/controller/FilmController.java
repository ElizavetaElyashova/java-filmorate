package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.debug("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film newFilm) throws NotFoundException {
        if (films.containsKey(newFilm.getId())) {
            log.debug("Изменение фильма {} на фильм {}", films.get(newFilm.getId()), newFilm);
            films.put(newFilm.getId(), newFilm);
            return newFilm;
        }
        log.warn("Фильм с id = {}  не найден", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
