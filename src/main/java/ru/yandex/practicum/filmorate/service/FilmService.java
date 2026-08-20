package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    @Getter
    @Autowired
    @Qualifier("filmDbStorage")
    private FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private UserStorage userStorage;
    private GenreDbStorage genreDbStorage;
    private FeedDbStorage feedDbStorage;
    private JdbcTemplate jdbc;

    private String updateLikes = "UPDATE films SET likes = likes + ? WHERE id = ?";
    private String addUserLiked = "INSERT INTO likes VALUES(?, ?)";
    private String deleteUserLiked = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    @Autowired
    public FilmService(FilmDbStorage filmStorage, UserDbStorage userStorage, GenreDbStorage genreDbStorage, FeedDbStorage feedDbStorage, JdbcTemplate jdbc) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreDbStorage = genreDbStorage;
        this.feedDbStorage = feedDbStorage;
        this.jdbc = jdbc;
    }


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
                    .eventType(new EventType(1))
                    .operation(new Operation(2))
                    .build());
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
                    .eventType(new EventType(1))
                    .operation(new Operation(1))
                    .build());
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

    public Film create(Film film) {
        if (film.getGenres() != null) {
            Set<Integer> genresIds = new HashSet<>(
                    film.getGenres().stream()
                            .map(Genre::getId)
                            .toList());
            for (int id : genresIds) {
                genreDbStorage.findById(id);
            }
            film = filmStorage.create(film);
            film.setGenres(
                    genreDbStorage.insertFilmGenres(film.getId(), genresIds));

        } else {
            film = filmStorage.create(film);
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
}
