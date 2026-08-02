package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    @Getter
    @Autowired
    @Qualifier("filmDbStorage")
    private FilmStorage filmStorage;


    public void addLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
        log.trace("Пользователь с id = {} ставит лайк фильму с id = {}", filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmStorage.deleteLike(filmId, userId);
        log.trace("Пользователь с id = {} удаляет лайк у фильма с id = {}", filmId, userId);
    }

    public List<Film> findPopular(int count) {
        log.trace("Возвращает популярные фильмы в количестве {}", count);
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikes).reversed())
                .limit(count)
                .toList();
    }
}
