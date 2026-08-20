package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.Collection;
import java.util.Comparator;

@Repository
@Slf4j
public class MpaDbStorage {
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private MpaRowMapper mapper;

    private String findAllQuery = "SELECT * FROM ratings;";
    private static String findByIdQuery = "SELECT * FROM ratings WHERE id = ?;";

    public Collection<Mpa> findAll() {
        return jdbc.query(findAllQuery, mapper).stream().sorted(Comparator.comparingInt(Mpa::getId)).toList();
    }

    public Mpa findById(Integer id) {
        try {
            return jdbc.queryForObject(findByIdQuery, mapper, id);
        } catch (DataAccessException e) {
            log.warn("Рейтинг с id = {} не найден.", id);
            throw new NotFoundException("Рейтинг с id = " + id + " не найден.");
        }
    }


}
