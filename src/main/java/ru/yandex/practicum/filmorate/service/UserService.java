package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
public class UserService {
    @Getter
    @Autowired
    @Qualifier("userDbStorage")
    private UserStorage userStorage;

    public void addFriend(Long id, Long friendId) {
        userStorage.addFriend(id, friendId);
        log.trace("Пользователь с id = {} добавляет в друзья пользователя с id = {}", id, friendId);
    }

    public void deleteFriend(Long id, Long friendId) {
        userStorage.deleteFriend(id, friendId);
        log.trace("Пользователь с id = {} удаляет из друзей пользователя с id = {}", id, friendId);
    }

    public List<User> findCommonFriends(Long id, Long otherId) {
        List<User> userFriends = userStorage.findFriends(id);
        List<User> otherUserFriends = userStorage.findFriends(otherId);
        log.trace("Возвращает общих друзей пользователей с id {} и {}", id, otherId);
        return userFriends.stream()
                .filter(u -> otherUserFriends.contains(u))
                .toList();
    }
}
