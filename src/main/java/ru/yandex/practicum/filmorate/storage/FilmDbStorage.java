package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
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
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmMapper;
    private final DirectorRowMapper directorMapper;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final DirectorDbStorage directorDbStorage;


    private String findByIdQuery = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films AS f JOIN ratings AS r ON f.rating_id = r.id WHERE f.id = ?;";
    private String findUsersLiked = "SELECT user_id FROM likes WHERE film_id = ?;";
    private String findAllQuery = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films f JOIN ratings r ON f.rating_id = r.id";
    private String insertFilmQuery = "INSERT INTO films(name, description, release_date, duration, likes, rating_id) " +
            "VALUES(?, ?, ?, ?, ?, ?);";
    private String insertFilmDirectors = "INSERT INTO film_director(film_id, director_id) VALUES(?, ?);";
    private String updateFilmQuery = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
    private String deleteFilmQuery = "DELETE FROM films WHERE ID = ?;";
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
    private String deleteFilmDirectorsQuery = """
            DELETE FROM film_director
            WHERE film_id = ?
            """;
    private String findCommonFilmsQuery =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
                    "FROM films f " +
                    "JOIN ratings r ON f.rating_id = r.id " +
                    "JOIN likes l1 ON l1.film_id = f.id " +
                    "JOIN likes l2 ON l2.film_id = f.id " +
                    "WHERE l1.user_id = ? AND l2.user_id = ? " +
                    "ORDER BY f.likes DESC, f.id ASC;";
    private final String findPopularByGenreQuery =
            "SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
                    "FROM films f " +
                    "JOIN ratings r ON f.rating_id = r.id " +
                    "JOIN film_genre fg ON fg.film_id = f.id " +
                    "WHERE fg.genre_id = ? " +
                    "ORDER BY f.likes DESC, f.id ASC " +
                    "LIMIT ?;";

    private final String findPopularByYearQuery =
            "SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
                    "FROM films f " +
                    "JOIN ratings r ON f.rating_id = r.id " +
                    "WHERE YEAR(f.release_date) = ? " +
                    "ORDER BY f.likes DESC, f.id ASC " +
                    "LIMIT ?;";

    private final String findPopularByGenreAndYearQuery =
            "SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
                    "FROM films f " +
                    "JOIN ratings r ON f.rating_id = r.id " +
                    "JOIN film_genre fg ON fg.film_id = f.id " +
                    "WHERE fg.genre_id = ? AND YEAR(f.release_date) = ? " +
                    "ORDER BY f.likes DESC, f.id ASC " +
                    "LIMIT ?;";

    private String findFilmByTitleAndByDirector = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films f " +
            "JOIN ratings r ON f.rating_id = r.id " +
            "LEFT JOIN film_director fd ON f.id = fd.film_id " +
            "LEFT JOIN directors d ON fd.director_id = d.id " +
            "WHERE (LOWER(f.name) LIKE ? OR LOWER(d.name) LIKE ?) " +
            "ORDER BY f.likes DESC, f.id ASC;";

    private String findFilmByTitle = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films f " +
            "JOIN ratings r ON f.rating_id = r.id " +
            "LEFT JOIN film_director fd ON f.id = fd.film_id " +
            "LEFT JOIN directors d ON fd.director_id = d.id " +
            "WHERE LOWER(f.name) LIKE ? " +
            "ORDER BY f.likes DESC, f.id ASC;";

    private String findFilmByDirector = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films f " +
            "JOIN ratings r ON f.rating_id = r.id " +
            "LEFT JOIN film_director fd ON f.id = fd.film_id " +
            "LEFT JOIN directors d ON fd.director_id = d.id " +
            "WHERE LOWER(d.name) LIKE ? " +
            "ORDER BY f.likes DESC, f.id ASC;";

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(findAllQuery, filmMapper);
        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
            film.setDirectors(directorDbStorage.findAllDirectorsByFilmId(film.getId()));
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
            film.setDirectors(directorDbStorage.findAllDirectorsByFilmId(id));
            log.trace("Фильм с id = {} найден", id);
            return film;
        } catch (DataAccessException e) {
            log.warn("Фильм с id = {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден.");
        }
    }

    @Override
    public List<Film> findCommonFilms(Long userId, Long friendId) {
        return jdbc.query(findCommonFilmsQuery, filmMapper, userId, friendId);
    }

    @Override
    public List<Film> findPopularByGenre(Long genreId, int count) {
        return jdbc.query(findPopularByGenreQuery, filmMapper, genreId.intValue(), count);
    }

    @Override
    public List<Film> findPopularByYear(int year, int count) {
        return jdbc.query(findPopularByYearQuery, filmMapper, year, count);
    }

    @Override
    public List<Film> findPopularByGenreAndYear(Long genreId, int year, int count) {
        return jdbc.query(findPopularByGenreAndYearQuery, filmMapper, genreId.intValue(), year, count);
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
        film.setDirectors(directorDbStorage.findAllDirectorsByFilmId(film.getId()));

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
        if (newFilm.getDirectors() == null || newFilm.getDirectors().isEmpty()) {
            jdbc.update(deleteFilmDirectorsQuery, newFilm.getId());
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
        jdbc.update(deleteFilmQuery, id);
        log.debug("Фильм с id = {} удален", id);
    }

    @Override
    public List<Film> search(String query, String by) {
        log.info("Поиск фильмов: query='{}', by='{}'", query, by);

        if (query == null || query.isBlank()) {
            log.warn("Пустой поисковый запрос");
            return List.of();
        }

        String searchPattern = "%" + query.toLowerCase() + "%";
        Set<String> bySet = Set.of(by.toLowerCase().split(","));

        boolean byTitle = bySet.contains("title");
        boolean byDirector = bySet.contains("director");

        if (!byTitle && !byDirector) {
            log.warn("Некорректный параметр 'by': {}", by);
            throw new IllegalArgumentException("Параметр 'by' должен содержать 'title', 'director' или оба значения через запятую.");
        }

        List<Film> films;

        if (byTitle && byDirector) {
            films = jdbc.query(findFilmByTitleAndByDirector, filmMapper, searchPattern, searchPattern);
        } else if (byTitle) {
            films = jdbc.query(findFilmByTitle, filmMapper, searchPattern);
        } else {
            films = jdbc.query(findFilmByDirector, filmMapper, searchPattern);
        }

        // Заполняем дополнительные данные для каждого фильма
        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
            film.setDirectors(directorDbStorage.findAllDirectorsByFilmId(film.getId()));
        }

        log.debug("Поиск вернул {} фильмов", films.size());
        return films;
    }
}
