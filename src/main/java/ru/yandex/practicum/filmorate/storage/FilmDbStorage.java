package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Qualifier("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private FilmRowMapper filmMapper;
    private MpaDbStorage mpaDbStorage;
    private GenreDbStorage genreDbStorage;
    private UserDbStorage userDbStorage;

    @Autowired
    public FilmDbStorage(MpaDbStorage mpaDbStorage, GenreDbStorage genreDbStorage, UserDbStorage userDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
        this.userDbStorage = userDbStorage;
    }

    private String findByIdQuery = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films AS f JOIN ratings AS r ON f.rating_id = r.id WHERE f.id = ?;";
    private String findUsersLiked = "SELECT user_id FROM likes WHERE film_id = ?;";
    private String findAllQuery = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films f JOIN ratings r ON f.rating_id = r.id";
    private String insertFilmQuery = "INSERT INTO films(name, description, release_date, duration, likes, rating_id) " +
            "VALUES(?, ?, ?, ?, ?, ?);";
    private String updateFilmQuery = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
    private String deleteFilmQuery = "DELETE FROM likes WHERE film_id = ?;\n" +
            "DELETE FROM film_genre WHERE film_id = ?;\n" +
            "DELETE FROM films WHERE ID = ?;";


    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(findAllQuery, filmMapper);
        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
        }
        return films;
    }

    @Override
    public Film findById(Long id) {
        try {
            Film film = jdbc.queryForObject(findByIdQuery, filmMapper, id);
            List<Genre> genres = genreDbStorage.findFilmGenres(id);
            Set<Long> userLikedIds = new HashSet<>(jdbc.queryForList(findUsersLiked, Long.class, id));
            film.setGenres(genres);
            film.setUsersLikedIds(userLikedIds);
            log.trace("Фильм с id = {} найден", id);
            return film;
        } catch (DataAccessException e) {
            log.warn("Фильм с id = {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден.");
        }
    }

    @Override
    public Film create(Film film) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        film.setMpa(mpaDbStorage.findById(film.getMpa().getId()));
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertFilmQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, film.getName());
            ps.setObject(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setObject(4, film.getDuration());
            ps.setObject(5, 0);
            ps.setObject(6, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKeyAs(Long.class));
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        Film oldFilm = findById(newFilm.getId());
        if (newFilm.getName() == null) {
            newFilm.setName(oldFilm.getName());
        }
        if (newFilm.getDescription() == null) {
            newFilm.setDescription(oldFilm.getDescription());
        }
        if (newFilm.getReleaseDate() == null) {
            newFilm.setReleaseDate(oldFilm.getReleaseDate());
        }
        if (newFilm.getDuration() == null) {
            newFilm.setDuration(oldFilm.getDuration());
        }
        if (newFilm.getMpa() == null) {
            newFilm.setMpa(oldFilm.getMpa());
        }
        if (newFilm.getGenres() == null) {
            newFilm.setGenres(oldFilm.getGenres());
        }
        jdbc.update(updateFilmQuery, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
                newFilm.getDuration(),  newFilm.getMpa().getId(), newFilm.getId());
        newFilm.setLikes(oldFilm.getLikes());
        newFilm.setUsersLikedIds(oldFilm.getUsersLikedIds());
        return newFilm;
    }

    @Override
    public void remove(Long id) {
        findById(id);
        jdbc.update(deleteFilmQuery, id, id, id);
        log.debug("Фильм с id = {} удален", id);
    }

}
