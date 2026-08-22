package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findById(Long id);

    Film create(@RequestBody @Valid Film film);

    Film update(@RequestBody @Valid Film newFilm);

    void remove(Long id);

    Director createDirector(String name);

    void insertFilmDirectors(Long filmId, Long directorId);

    List<Director> findAllDirectors();

    Optional<Director> findDirectorById(Long id);

    Director updateDirector(Director updatedDirector);

    void deleteDirector(Long id);

    List<Film> findAllDirectorsFilmsSorted(Long directorId, String sortType);
}
