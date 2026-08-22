package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {GenreDbStorage.class, GenreRowMapper.class,
        FilmDbStorage.class, FilmRowMapper.class,
        MpaDbStorage.class, MpaRowMapper.class,
        UserDbStorage.class, UserRowMapper.class, DirectorRowMapper.class})
public class GenreDbStorageTests {
    private final GenreDbStorage genreDbStorage;
    private final FilmDbStorage filmDbStorage;
    private Film basicFilm;
    private Genre genre1;
    private Genre genre2;

    @BeforeEach
    void beforeEach() {
        genre1 = new Genre();
        genre1.setId(2);
        genre1.setName("Драма");
        genre2 = new Genre();
        genre2.setId(3);
        genre2.setName("Мультфильм");

        Mpa mpa = new Mpa();
        mpa.setId(1);
        basicFilm = Film.builder()
                .name("movie")
                .description("description")
                .releaseDate(LocalDate.of(1895, Month.DECEMBER, 28))
                .duration(100)
                .mpa(mpa)
                .build();
    }

    @Test
    public void testGenreFindById() {
        assertThat(genreDbStorage.findById(genre1.getId())).isEqualTo(genre1);
    }

    @Test
    public void testMpaFindAll() {
        assertThat(genreDbStorage.findAll().size()).isEqualTo(6);
        assertThat(genreDbStorage.findAll()).contains(genre1, genre2);
    }

    @Test
    public void testInsertFilmGenres() {
        Film film = filmDbStorage.create(basicFilm);
        genreDbStorage.insertFilmGenres(film.getId(), Set.of(genre1.getId(), genre2.getId()));
        film = filmDbStorage.findById(film.getId());
        assertThat(film.getGenres()).contains(genre1, genre2);
    }

    @Test
    public void testFindFilmGenres() {
        Film film = filmDbStorage.create(basicFilm);
        genreDbStorage.insertFilmGenres(film.getId(), Set.of(genre1.getId(), genre2.getId()));
        assertThat(genreDbStorage.findFilmGenres(film.getId())).contains(genre1, genre2);
    }

}
