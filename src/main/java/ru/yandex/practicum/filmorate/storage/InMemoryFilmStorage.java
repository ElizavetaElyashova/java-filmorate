package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component("inMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    @Qualifier("inMemoryUserStorage")
    private final UserStorage userStorage;

    @Autowired
    public InMemoryFilmStorage(@Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film findById(Long id) {
        if (films.get(id) == null) {
            log.warn("Фильм с id = {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        log.trace("Фильм с id = {} найден", id);
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        film.setLikes(0);
        film.setUsersLikedIds(new HashSet<>());
        films.put(film.getId(), film);
        log.debug("Фильм {} добавлен", film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (films.containsKey(newFilm.getId())) {
            log.debug("Изменение фильма {} на фильм {}", films.get(newFilm.getId()), newFilm);
            films.put(newFilm.getId(), newFilm);
            return newFilm;
        }
        log.warn("Фильм с id = {} не найден", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    public void remove(Long id) {
        if (findById(id) != null) {
            films.remove(id);
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void addLike(Long filmId, Long userId) {
        if (userStorage.findById(userId) != null) {
            Film film = findById(filmId);
            if (film.getUsersLikedIds().add(userId)) {
                log.debug("Количество лайков у фильма с id = {} было {}", film.getId(), film.getLikes());
                film.setLikes(film.getLikes() + 1);
                log.debug("Количество лайков у фильма с id = {} стало {}", film.getId(), film.getLikes());
            } else {
                log.info("Пользователь с id = {} уже поставил лайк фильму с id = {}", userId, filmId);
            }
        } else {
            log.warn("Пользователь с id = {} не найден", filmId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    public void deleteLike(Long filmId, Long userId) {
        if (userStorage.findById(userId) != null) {
            Film film = findById(filmId);
            if (film.getUsersLikedIds().remove(userId)) {
                log.debug("Количество лайков у фильма с id = {} было {}", film.getId(), film.getLikes());
                film.setLikes(film.getLikes() - 1);
                log.debug("Количество лайков у фильма с id = {} стало {}", film.getId(), film.getLikes());
            } else {
                log.info("Пользователь с id = {} уже удалил лайк у фильма с id = {}", userId, filmId);
            }
        } else {
            log.warn("Пользователь с id = {} не найден", filmId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }
}
