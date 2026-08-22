package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@Qualifier("filmDbStorage")
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmMapper;
    private final DirectorRowMapper directorMapper;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;


    private String findByIdQuery = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films AS f JOIN ratings AS r ON f.rating_id = r.id WHERE f.id = ?;";
    private String findUsersLiked = "SELECT user_id FROM likes WHERE film_id = ?;";
    private String findAllQuery = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films f JOIN ratings r ON f.rating_id = r.id";
    private String insertFilmQuery = "INSERT INTO films(name, description, release_date, duration, likes, rating_id) " +
            "VALUES(?, ?, ?, ?, ?, ?);";
    private String insertFilmDirectors = "INSERT INTO film_director(film_id, director_id) VALUES(?, ?);";
    private String insertDirector = "INSERT INTO directors(name) VALUES(?);";
    private String updateFilmQuery = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
    private String deleteFilmQuery = "DELETE FROM likes WHERE film_id = ?;\n" +
            "DELETE FROM film_genre WHERE film_id = ?;\n" +
            "DELETE FROM films WHERE ID = ?;";
    private String findAllDirectorsQuery = "SELECT * FROM directors";
    private String findDirectorByIdQuery = "SELECT * FROM directors WHERE id = ?";
    private String updateDirectorQuery = "UPDATE directors SET name = ? WHERE id = ?";
    private String deleteDirectorQuery = "DELETE FROM directors WHERE id = ?";
    private String insertFilmDirectorQuery = """
            INSERT INTO film_director (film_id, director_id)
            SELECT ?, ?
            WHERE NOT EXISTS (
                SELECT 1
                FROM film_director
                WHERE film_id = ?
                  AND director_id = ?
            )
            """;
    private String findAllDirectorsByFilmId = """
            SELECT d.id, d.name
            FROM films AS f
            JOIN film_director AS fd ON fd.film_id = f.id
            JOIN directors AS d ON d.id = fd.director_id
            WHERE f.id = ?
            """;
    private String findAllDirectorsFilms = """
            SELECT f.id, f.name, f.release_date, f.description, f.duration, f.likes, f.rating_id, r.name mpa, d.name director
            FROM directors AS d
            JOIN film_director AS fd ON d.id = fd.director_id
            JOIN films AS f ON f.id = fd.film_id
            JOIN ratings AS r ON f.rating_id = r.id
            WHERE d.id = ?
            """;
    private String deleteFilmDirectorsQuery = """
            DELETE FROM film_director
            WHERE film_id = ?
            """;
    private final String LIKES = "LIKES";

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(findAllQuery, filmMapper);
        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
            film.setDirectors(jdbc.query(findAllDirectorsByFilmId, directorMapper, film.getId()));
        }
        return films;
    }

    public List<Film> findAllDirectorsFilmsSorted(Long directorId, String sortType) {
        List<Film> films = jdbc.query(findAllDirectorsFilms, filmMapper, directorId);
        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
            film.setDirectors(jdbc.query(findAllDirectorsByFilmId, directorMapper, film.getId()));
        }
        if (sortType.equalsIgnoreCase(LIKES)) {
            return films.stream().sorted(Comparator.comparingInt(Film::getLikes).reversed()).toList();
        }
        return films.stream().sorted(Comparator.comparing(Film::getReleaseDate)).toList();
    }

    @Override
    public Film findById(Long id) {
        try {
            Film film = jdbc.queryForObject(findByIdQuery, filmMapper, id);
            List<Genre> genres = genreDbStorage.findFilmGenres(id);
            Set<Long> userLikedIds = new HashSet<>(jdbc.queryForList(findUsersLiked, Long.class, id));
            film.setGenres(genres);
            film.setUsersLikedIds(userLikedIds);
            film.setDirectors(jdbc.query(findAllDirectorsByFilmId, directorMapper, id));
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

        if (film.getDirectors() != null) {
            addFilmDirectors(film.getId(), film.getDirectors());
        }
        film.setDirectors(jdbc.query(findAllDirectorsByFilmId, directorMapper, film.getId()));

        return film;
    }

    private void addFilmDirectors(Long filmId, List<Director> directorsList) {
        jdbc.batchUpdate(insertFilmDirectorQuery, directorsList, directorsList.size(),
                (ps, director) -> {
                    ps.setLong(1, filmId);
                    ps.setLong(2, director.getId());
                    ps.setLong(3, filmId);
                    ps.setLong(4, director.getId());
                }
        );
    }

    public void insertFilmDirectors(Long filmId, Long directorId) {
        jdbc.update(insertFilmDirectors, filmId, directorId);
    }

    public Director createDirector(String name) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertDirector, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, name);
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        if (id != null) {
            return new Director(id, name);
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    public List<Director> findAllDirectors() {
        return jdbc.query(findAllDirectorsQuery, directorMapper);
    }

    public Optional<Director> findDirectorById(Long id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(findDirectorByIdQuery, directorMapper, id));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public Director updateDirector(Director updatedDirector) {
        if (jdbc.update(updateDirectorQuery, updatedDirector.getName(), updatedDirector.getId()) != 1) {
            throw new InternalServerException("Не удалось обновить данные о режиссере с id = " +
                    updatedDirector.getId().toString());
        }
        return updatedDirector;
    }

    public void deleteDirector(Long id) {
        if (jdbc.update(deleteDirectorQuery, id) != 1) {
            throw new InternalServerException("Не удалось удалить режиссера с id = " + id.toString());
        }
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
        if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
            jdbc.update(deleteFilmDirectorsQuery, newFilm.getId());
            addFilmDirectors(newFilm.getId(), newFilm.getDirectors());
        }
        jdbc.update(updateFilmQuery, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
                newFilm.getDuration(), newFilm.getMpa().getId(), newFilm.getId());
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
