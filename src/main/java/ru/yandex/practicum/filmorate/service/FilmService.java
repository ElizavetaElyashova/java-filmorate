package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicateException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
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
public class FilmService {
    @Getter
    @Autowired
    @Qualifier("filmDbStorage")
    private FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private UserStorage userStorage;
    private GenreDbStorage genreDbStorage;
    private JdbcTemplate jdbc;

    private String updateLikes = "UPDATE films SET likes = likes + ? WHERE id = ?";
    private String addUserLiked = "INSERT INTO likes VALUES(?, ?)";
    private String deleteUserLiked = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    @Autowired
    public FilmService(FilmDbStorage filmStorage, UserDbStorage userStorage, GenreDbStorage genreDbStorage, JdbcTemplate jdbc) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreDbStorage = genreDbStorage;
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
            log.trace("Пользователь с id = {} ставит лайк фильму с id = {}", filmId, userId);
        }
    }

    public void deleteLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        if (film.getUsersLikedIds().contains(userId)) {
            jdbc.update(updateLikes, -1, filmId);
            jdbc.update(deleteUserLiked, filmId, userId);
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
        if (film.getDirectors() != null) {

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

    public Director createDirector(String name) {
        Set<String> directorsNames = filmStorage.findAllDirectors().stream()
                .map(director -> director.getName().toLowerCase())
                .collect(Collectors.toSet());
        if (directorsNames.contains(name.toLowerCase()))
            throw new DuplicateException("Режиссер с таким именем уже содержится в БД");
        return filmStorage.createDirector(name);
    }

    public List<Director> findAllDirectors() {
        return filmStorage.findAllDirectors();
    }

    public Director findDirectorById(Long id) {
        return filmStorage.findDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Не удалось найти режиссера с id = " + id.toString()));
    }

    public Director updateDirector(Director updatedDirector) {
        if (!isDirectorExist(updatedDirector.getId())) {
            throw new NotFoundException("Отсутствует режиссер с id = " + updatedDirector.getId().toString());
        }

        return filmStorage.updateDirector(updatedDirector);
    }

    public void deleteDirector(Long id) {
        if (!isDirectorExist(id)) {
            throw new NotFoundException("Отсутствует режиссер с id = " + id.toString());
        }

        filmStorage.deleteDirector(id);
    }

    private boolean isDirectorExist(Long id) {
        return filmStorage.findAllDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toSet()).contains(id);
    }

    public List<Film> findAllDirectorsFilmsSorted(Long directorId, String sortType){
        if (isDirectorExist(directorId)) {
            return filmStorage.findAllDirectorsFilmsSorted(directorId, sortType);
        } throw new NotFoundException("Отсутствует режиссер с id = " + directorId.toString());
    }
}
