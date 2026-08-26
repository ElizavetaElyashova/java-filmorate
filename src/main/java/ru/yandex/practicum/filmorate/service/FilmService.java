package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    @Getter
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreDbStorage;
    private final DirectorDbStorage directorStorage;
    private final FeedDbStorage feedDbStorage;
    private final JdbcTemplate jdbc;

    private String updateLikes = "UPDATE films SET likes = likes + ? WHERE id = ?";
    private String addUserLiked = "INSERT INTO likes VALUES(?, ?)";
    private String deleteUserLiked = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    public void addLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        if (film.getUsersLikedIds().contains(userId)) {
            log.info("Пользователь с id = {} уже поставил лайк фильму с id = {}", userId, filmId);
        } else {
            jdbc.update(updateLikes, 1, filmId);
            jdbc.update(addUserLiked, filmId, userId);
            feedDbStorage.create(Event.builder()
                    .userId(userId)
                    .entityId(filmId)
                    .build(), 1, 2);
            log.trace("Пользователь с id = {} ставит лайк фильму с id = {}", filmId, userId);
        }
    }

    public void deleteLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        if (film.getUsersLikedIds().contains(userId)) {
            jdbc.update(updateLikes, -1, filmId);
            jdbc.update(deleteUserLiked, filmId, userId);
            feedDbStorage.create(Event.builder()
                    .userId(userId)
                    .entityId(filmId)
                    .build(), 1, 1);
        } else {
            log.info("Пользователь с id = {} уже удалил лайк у фильма с id = {}", userId, filmId);
        }
    }

    public List<Film> findPopular(int count) {
        log.trace("Возвращает популярные фильмы в количестве {}", count);
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikes).reversed())
                .limit(count)
                .toList();
    }

    public List<Film> findCommonFilms(Long userId, Long friendId) {
        // Проверяем существование пользователей
        userStorage.findById(userId);
        userStorage.findById(friendId);

        List<Film> films = filmStorage.findCommonFilms(userId, friendId);

        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
        }

        log.trace("Возвращает общие популярные фильмы в количестве {}", films.size());
        return films;
    }

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
        log.debug("Фильм {} добавлен", film);
        return film;
    }

    public Film update(Film newFilm) {
        Film oldFilm = filmStorage.findById(newFilm.getId());
        if (newFilm.getGenres() == null) {
            newFilm.setGenres(oldFilm.getGenres());
        }
        for (Genre genre : newFilm.getGenres()) {
            genreDbStorage.findById(genre.getId());
        }
        newFilm = filmStorage.update(newFilm);
        genreDbStorage.updateFilmGenres(newFilm.getId(), newFilm.getGenres().stream().map(Genre::getId).collect(Collectors.toSet()));
        return newFilm;
    }

    private boolean isDirectorExist(Long id) {
        return directorStorage.findAllDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toSet()).contains(id);
    }

    public List<Film> findAllDirectorsFilmsSorted(Long directorId, String sortType) {
        if (isDirectorExist(directorId)) {
            return directorStorage.findAllDirectorsFilmsSorted(directorId, sortType);
        }
        throw new NotFoundException("Отсутствует режиссер с id = " + directorId.toString());
    }

    public List<Film> search(String query, String by) {
        return filmStorage.search(query, by);
    }
}
