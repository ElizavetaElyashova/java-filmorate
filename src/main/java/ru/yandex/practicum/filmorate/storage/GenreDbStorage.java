package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@Slf4j
public class GenreDbStorage {
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private GenreRowMapper mapper;

    private String findAllQuery = "SELECT * FROM genres;";
    private String findByIdQuery = "SELECT * FROM genres WHERE id = ?;";
    private String findFilmGenres = "SELECT * FROM genres WHERE id IN (SELECT genre_id FROM film_genre WHERE film_id = ?);";
    private String insertFilmGenres = "INSERT INTO film_genre(film_id, genre_id) VALUES(?, ?);";

    public Collection<Genre> findAll() {
        return jdbc.query(findAllQuery, mapper).stream().sorted(Comparator.comparingInt(Genre::getId)).toList();
    }

    public Genre findById(Integer id) {
        try {
            return jdbc.queryForObject(findByIdQuery, mapper, id);
        } catch (DataAccessException e) {
            log.warn("Жанр с id = {} не найден.", id);
            throw new NotFoundException("Жанр с id = " + id + " не найден.");
        }
    }

    public List<Genre> findFilmGenres(long id) {
        return jdbc.query(findFilmGenres, mapper, id);
    }

    private void insertFilmGenre(long filmId, int genreId) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertFilmGenres, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, filmId);
            ps.setObject(2, genreId);
            return ps;
        }, keyHolder);
        log.debug("Фильму с id = {} добавлен жанр с id = {}", filmId, genreId);
    }

    public List<Genre> insertFilmGenres(long filmId, Set<Integer> genresIds) {
        List<Genre> genres = new ArrayList<>();
        for (Integer genre_id : genresIds) {
            genres.add(findById(genre_id));
            insertFilmGenre(filmId, genre_id);
        }
        return genres;
    }

}
