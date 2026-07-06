package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    @Getter
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public void addLike(Long filmId, Long userId) {
        if (userService.getUserStorage().findById(userId) != null) {
            log.trace("Пользователь с id = {} ставит лайк фильму с id = {}", filmId, userId);
            Film film = filmStorage.findById(filmId);
            log.debug("Количество лайков у фильма с id = {} было {}", film.getId(), film.getLikes());
            film.setLikes(film.getLikes() + 1);
            log.debug("Количество лайков у фильма с id = {} стало {}", film.getId(), film.getLikes());
            film.getUsersLikedIds().add(userId);
        } else {
            log.warn("Пользователь с id = {} не найден", filmId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    public void deleteLike(Long filmId, Long userId) {
        if (userService.getUserStorage().findById(userId) != null) {
            log.trace("Пользователь с id = {} удаляет лайк у фильма с id = {}", filmId, userId);
            Film film = filmStorage.findById(filmId);
            log.debug("Количество лайков у фильма с id = {} было {}", film.getId(), film.getLikes());
            film.setLikes(film.getLikes() - 1);
            log.debug("Количество лайков у фильма с id = {} стало {}", film.getId(), film.getLikes());
            film.getUsersLikedIds().remove(userId);
        } else {
            log.warn("Пользователь с id = {} не найден", filmId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    public List<Film> findPopular(int count) {
        log.trace("Возвращает популярные фильмы в количестве {}", count);
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikes).reversed())
                .limit(count)
                .toList();
    }
}
