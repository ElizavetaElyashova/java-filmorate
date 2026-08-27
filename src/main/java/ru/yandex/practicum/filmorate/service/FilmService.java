package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreDbStorage;
    private final DirectorDbStorage directorStorage;
    private final FeedDbStorage feedDbStorage;
    private final MpaDbStorage mpaDbStorage;

    @Transactional
    public void addLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        if (film.getUsersLikedIds().contains(userId)) {
            log.info("Пользователь с id = {} уже поставил лайк фильму с id = {}", userId, filmId);
        } else {
            filmStorage.updateFilmLikes(filmId, 1);
            userStorage.addUserLiked(filmId, userId);
            log.trace("Пользователь с id = {} ставит лайк фильму с id = {}", filmId, userId);
        }
        feedDbStorage.create(Event.builder()
                .userId(userId)
                .entityId(filmId)
                .build(), 1, 2);
    }

    @Transactional
    public void deleteLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        if (film.getUsersLikedIds().contains(userId)) {
            filmStorage.updateFilmLikes(filmId, -1);
            userStorage.deleteUserLiked(filmId, userId);
        } else {
            log.info("Пользователь с id = {} уже удалил лайк у фильма с id = {}", userId, filmId);
        }
        feedDbStorage.create(Event.builder()
                .userId(userId)
                .entityId(filmId)
                .build(), 1, 1);
    }

    public List<Film> findPopular(int count, Long genreId, Integer year) {
        boolean hasGenre = genreId != null;
        boolean hasYear = year != null;

        List<Film> films;

        if (!hasGenre && !hasYear) {
            // /films/popular?count=...
            films = findAll().stream()
                    .sorted(Comparator.comparingInt(Film::getLikes).reversed())
                    .limit(count)
                    .toList();
            return films;
        }

        if (hasGenre && !hasYear) {
            // /films/popular?count=...&genreId=...
            films = filmStorage.findPopularByGenre(genreId, count);
        } else if (!hasGenre && hasYear) {
            // /films/popular?count=...&year=...
            films = filmStorage.findPopularByYear(year, count);
        } else {
            // /films/popular?count=...&genreId=...&year=...
            genreDbStorage.findById(genreId.intValue());
            films = filmStorage.findPopularByGenreAndYear(genreId, year, count);
        }

        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
            film.setDirectors(directorStorage.findAllDirectorsByFilmId(film.getId()));
        }
        return films;
    }

    public List<Film> findCommonFilms(Long userId, Long friendId) {
        // Проверяем существование пользователей
        userStorage.findById(userId);
        userStorage.findById(friendId);

        List<Film> films = filmStorage.findCommonFilms(userId, friendId);

        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
            film.setDirectors(directorStorage.findAllDirectorsByFilmId(film.getId()));
        }

        log.trace("Возвращает общие популярные фильмы в количестве {}", films.size());
        return films;
    }

    @Transactional
    public Film create(Film film) {
        film = filmStorage.create(film);
        if (film.getGenres() != null) {
            Set<Integer> genresIds = new HashSet<>(
                    film.getGenres().stream()
                            .map(Genre::getId)
                            .toList());
            for (int id : genresIds) {
                genreDbStorage.findById(id);
            }
            film.setGenres(genreDbStorage.insertFilmGenres(film.getId(), genresIds));
        }
        film.setMpa(mpaDbStorage.findById(film.getMpa().getId()));
        film.setDirectors(directorStorage.findAllDirectorsByFilmId(film.getId()));
        log.debug("Фильм {} добавлен", film);
        return film;
    }

    public Film update(Film newFilm) {
        Film oldFilm = findById(newFilm.getId());
        if (newFilm.getGenres() == null) {
            newFilm.setGenres(oldFilm.getGenres());
        } else {
            for (Genre genre : newFilm.getGenres()) {
                genreDbStorage.findById(genre.getId());
            }
            newFilm.setGenres(genreDbStorage.updateFilmGenres(newFilm.getId(), newFilm.getGenres().stream().map(Genre::getId).collect(Collectors.toSet())));
        }

        newFilm = filmStorage.update(newFilm);
        return newFilm;
    }

    public void remove(Long filmId) {
        filmStorage.remove(filmId);
    }

    private boolean isDirectorExist(Long id) {
        return directorStorage.findAllDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toSet()).contains(id);
    }

    public List<Film> findAllDirectorsFilmsSorted(Long directorId, String sortType) {
        if (isDirectorExist(directorId)) {
            List<Film> films = directorStorage.findAllDirectorsFilmsSorted(directorId, sortType);
            for (Film film : films) {
                film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
            }
            return films;
        }
        throw new NotFoundException("Отсутствует режиссер с id = " + directorId.toString());
    }

    public List<Film> search(String query, String by) {
        List<Film> films = filmStorage.search(query, by);
        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
            film.setDirectors(directorStorage.findAllDirectorsByFilmId(film.getId()));
        }
        return films;
    }

    public Film findById(Long id) {
        Film film = filmStorage.findById(id);
        film.setGenres(genreDbStorage.findFilmGenres(id));
        film.setDirectors(directorStorage.findAllDirectorsByFilmId(id));
        return film;
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
            film.setDirectors(directorStorage.findAllDirectorsByFilmId(film.getId()));
        }
        return films;
    }
}
