package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Qualifier("userDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private UserRowMapper mapper;

    private String FIND_ALL_QUERY = "SELECT * FROM users;";
    private String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?;";
    private String INSERT_USER_QUERY = "INSERT INTO users(email, login, name, birthday) VALUES(?, ?, ?, ?);";
    private String DELETE_USER_QUERY = "DELETE FROM friends WHERE user_id = ? OR friend_id = ?;\n" +
            "DELETE FROM likes WHERE user_id = ?;\n" +
            "DELETE FROM users WHERE id = ?;";
    private String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ?" +
            "WHERE id = ?";
    private String FIND_FRIENDS_QUERY = "SELECT * FROM users WHERE id IN " +
            "(SELECT friend_id FROM friends WHERE user_id = ?);";
    private String ADD_FRIEND_QUERY = "INSERT INTO friends(user_id, friend_id) VALUES(?, ?);";
    private String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?;";


    @Override
    public Collection<User> findAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public User create(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, user.getEmail());
            ps.setObject(2, user.getLogin());
            ps.setObject(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        user.setId(id);
        user.setFriendsIds(new HashSet<>());
        log.debug("Добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User newUser) {
        User oldUser = findById(newUser.getId());
        if (newUser.getLogin() == null) {
            newUser.setLogin(oldUser.getLogin());
        }
        if (newUser.getEmail() == null) {
            newUser.setEmail(oldUser.getEmail());
        }
        if (newUser.getBirthday() == null) {
            newUser.setBirthday(oldUser.getBirthday());
        }
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        jdbc.update(UPDATE_USER_QUERY, newUser.getEmail(), newUser.getLogin(),
                newUser.getName(), newUser.getBirthday(), newUser.getId());
        log.debug("Изменение пользователя {} на пользователя {}", oldUser, newUser);
        return newUser;
    }

    @Override
    public User findById(Long id) {
        try {
            User user = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            Set<Long> friends = new HashSet<>(
                    jdbc.query(FIND_FRIENDS_QUERY, mapper, id).stream()
                            .map(User::getId)
                            .toList());
            user.setFriendsIds(friends);
            log.trace("Пользователь с id = {} найден", id);
            return user;
        } catch (DataAccessException e) {
            log.warn("Пользователь с id = {} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }
    }

    @Override
    public List<User> findFriends(Long id) {
        findById(id);
        log.trace("Возвращает друзей пользователя с id = {}.", id);
        return jdbc.query(FIND_FRIENDS_QUERY, mapper, id);
    }

    @Override
    public void remove(Long id) {
        findById(id);
        jdbc.update(DELETE_USER_QUERY, id, id, id, id);
        log.info("Пользователь с id = {} был удален.", id);
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        findById(id);
        findById(friendId);
        jdbc.update(ADD_FRIEND_QUERY, id, friendId);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        findById(id);
        findById(friendId);
        jdbc.update(DELETE_FRIEND_QUERY, id, friendId);
    }
}
