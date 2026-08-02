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

    private String FIND_BY_ID_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films AS f JOIN ratings AS r ON f.rating_id = r.id WHERE f.id = ?;";
    private String FIND_USERS_LIKED = "SELECT user_id FROM likes WHERE film_id = ?;";
    private String UPDATE_LIKES = "UPDATE films SET likes = likes + ? WHERE id = ?";
    private String ADD_USER_LIKED = "INSERT INTO likes VALUES(?, ?)";
    private String DELETE_USER_LIKED = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private String FIND_ALL_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name AS mpa " +
            "FROM films f JOIN ratings r ON f.rating_id = r.id";
    private String INSERT_FILM_QUERY = "INSERT INTO films(name, description, release_date, duration, likes, rating_id) " +
            "VALUES(?, ?, ?, ?, ?, ?);";
    private String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ? WHERE id = ?";
    private String DELETE_FILM_QUERY = "DELETE FROM likes WHERE film_id = ?;\n" +
            "DELETE FROM film_genre WHERE film_id = ?;\n" +
            "DELETE FROM films WHERE ID = ?;";


    @Override
    public Collection<Film> findAll() {
        return jdbc.query(FIND_ALL_QUERY, filmMapper);
    }

    @Override
    public Film findById(Long id) {
        try {
            Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, filmMapper, id);
            List<Genre> genres = genreDbStorage.findFilmGenres(id);
            Set<Long> userLikedIds = new HashSet<>(jdbc.queryForList(FIND_USERS_LIKED, Long.class, id));
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
            PreparedStatement ps = con.prepareStatement(INSERT_FILM_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, film.getName());
            ps.setObject(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setObject(4, film.getDuration());
            ps.setObject(5, 0);
            ps.setObject(6, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKeyAs(Long.class));

        if (film.getGenres() != null) {
            Set<Integer> genresIds = new HashSet<>(
                    film.getGenres().stream()
                            .map(Genre::getId)
                            .toList());
            film.setGenres(
                    genreDbStorage.insertFilmGenres(film.getId(), genresIds));

        }
        log.debug("Фильм {} добавлен", film);
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
        jdbc.update(UPDATE_FILM_QUERY, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
                newFilm.getDuration(), newFilm.getId());
        newFilm.setLikes(oldFilm.getLikes());
        newFilm.setUsersLikedIds(oldFilm.getUsersLikedIds());
        newFilm.setGenres(oldFilm.getGenres());
        newFilm.setMpa(oldFilm.getMpa());
        log.debug("Изменение фильма {} на фильм {}", oldFilm, newFilm);
        return newFilm;
    }

    @Override
    public void remove(Long id) {
        findById(id);
        jdbc.update(DELETE_FILM_QUERY, id, id, id);
        log.debug("Фильм с id = {} удален", id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        userDbStorage.findById(userId);
        Film film = findById(filmId);
        if (film.getUsersLikedIds().contains(userId)) {
            log.info("Пользователь с id = {} уже поставил лайк фильму с id = {}", userId, filmId);
        } else {
            jdbc.update(UPDATE_LIKES, 1, filmId);
            jdbc.update(ADD_USER_LIKED, filmId, userId);
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        userDbStorage.findById(userId);
        Film film = findById(filmId);
        if (film.getUsersLikedIds().contains(userId)) {
            jdbc.update(UPDATE_LIKES, -1, filmId);
            jdbc.update(DELETE_USER_LIKED, filmId, userId);
        } else {
            log.info("Пользователь с id = {} уже удалил лайк у фильма с id = {}", userId, filmId);
        }
    }
}
