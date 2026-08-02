package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {MpaDbStorage.class, MpaRowMapper.class})
public class MpaDbStorageTests {
    private final MpaDbStorage mpaDbStorage;

    @Test
    public void testMpaFindById() {
        Mpa pg = new Mpa();
        pg.setId(2);
        pg.setName("PG");
        assertThat(mpaDbStorage.findById(2)).isEqualTo(pg);
    }

    @Test
    public void testMpaFindAll() {
        Mpa r = new Mpa();
        r.setId(4);
        r.setName("R");
        assertThat(mpaDbStorage.findAll().size()).isEqualTo(5);
        assertThat(mpaDbStorage.findAll()).contains(r);
    }
}
