package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage {
    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorMapper;
    private final FilmRowMapper filmMapper;
    private final GenreDbStorage genreDbStorage;


    private String insertDirector = "INSERT INTO directors(name) VALUES(?)";
    private String findAllDirectorsQuery = "SELECT * FROM directors";
    private String findDirectorByIdQuery = "SELECT * FROM directors WHERE id = ?";
    private String updateDirectorQuery = "UPDATE directors SET name = ? WHERE id = ?";
    private String deleteDirectorQuery = "DELETE FROM directors WHERE id = ?";
    private String findAllDirectorsFilms = """
            SELECT f.id, f.name, f.release_date, f.description, f.duration, f.likes, f.rating_id, r.name mpa, d.name director
            FROM directors AS d
            JOIN film_director AS fd ON d.id = fd.director_id
            JOIN films AS f ON f.id = fd.film_id
            JOIN ratings AS r ON f.rating_id = r.id
            WHERE d.id = ?
            """;
    private String findAllDirectorsByFilmIdQuery = """
            SELECT d.id, d.name
            FROM films AS f
            JOIN film_director AS fd ON fd.film_id = f.id
            JOIN directors AS d ON d.id = fd.director_id
            WHERE f.id = ?
            """;
    private static final String LIKES = "LIKES";


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

    public List<Film> findAllDirectorsFilmsSorted(Long directorId, String sortType) {
        List<Film> films = jdbc.query(findAllDirectorsFilms, filmMapper, directorId);
        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
            film.setDirectors(findAllDirectorsByFilmId(film.getId()));
        }
        if (sortType.equalsIgnoreCase(LIKES)) {
            return films.stream().sorted(Comparator.comparingInt(Film::getLikes).reversed()).toList();
        }
        return films.stream().sorted(Comparator.comparing(Film::getReleaseDate)).toList();
    }

    public List<Director> findAllDirectorsByFilmId(Long filmId) {
        return jdbc.query(findAllDirectorsByFilmIdQuery, directorMapper, filmId);
    }
}
