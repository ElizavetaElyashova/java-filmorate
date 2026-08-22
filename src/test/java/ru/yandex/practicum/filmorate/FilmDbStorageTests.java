package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.*;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {GenreDbStorage.class, GenreRowMapper.class,
        FilmDbStorage.class, FilmRowMapper.class,
        MpaDbStorage.class, MpaRowMapper.class,
        UserDbStorage.class, UserRowMapper.class, DirectorRowMapper.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilmDbStorageTests {
    private final FilmDbStorage filmDbStorage;
    private Film film1;
    private Film film2;

    @BeforeEach
    void beforeEach() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film1 = Film.builder()
                .name("movie")
                .description("description")
                .releaseDate(LocalDate.of(1895, Month.DECEMBER, 28))
                .duration(100)
                .mpa(mpa)
                .build();
        film2 = Film.builder()
                .name("cien")
                .description("años")
                .releaseDate(LocalDate.of(1987, Month.APRIL, 11))
                .duration(100)
                .mpa(mpa)
                .build();
    }

    @Test
    @Order(1)
    public void testFilmCreate() {
        film1 = filmDbStorage.create(film1);
        assertThat(filmDbStorage.findAll()).contains(film1);
    }

    @Test
    public void testFilmUpdate() {
        long id = filmDbStorage.create(film1).getId();
        film1.setName("Updated");
        film1.setDuration(123);
        Film updatedFilm = filmDbStorage.update(film1);
        assertThat(updatedFilm).isEqualTo(filmDbStorage.findById(id));
    }

    @Test
    public void testFilmFindById() {
        Long id = filmDbStorage.create(film1).getId();
        film1.setId(id);
        System.out.println(film1.getUsersLikedIds());
        assertThat(film1).isEqualTo(filmDbStorage.findById(id));
    }

    @Test
    public void testFilmFindAll() {
        film1 = filmDbStorage.create(film1);
        film2 = filmDbStorage.create(film2);
        assertThat(filmDbStorage.findAll()).contains(film1, film2);
    }

    @Test
    public void testFilmRemove() {
        film1 = filmDbStorage.create(film1);
        film2 = filmDbStorage.create(film2);
        filmDbStorage.remove(film1.getId());
        assertThat(filmDbStorage.findAll()).containsOnly(film2);
    }
}
