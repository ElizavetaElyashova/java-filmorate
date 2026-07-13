package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    @Getter
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long id, Long friendId) {
        User user = userStorage.findById(id);
        User friend = userStorage.findById(friendId);
        log.trace("Пользователь с id = {} добавляет в друзья пользователя с id = {}", id, friendId);
        user.getFriendsIds().add(friendId);
        friend.getFriendsIds().add(id);
    }

    public void deleteFriend(Long id, Long friendId) {
        User user = userStorage.findById(id);
        User friend = userStorage.findById(friendId);
        log.trace("Пользователь с id = {} удаляет из друзей пользователя с id = {}", id, friendId);
        user.getFriendsIds().remove(friendId);
        friend.getFriendsIds().remove(id);
    }

    public List<User> findCommonFriends(Long id, Long otherId) {
        Set<Long> userFriends = userStorage.findById(id).getFriendsIds();
        Set<Long> otherUserFriends = userStorage.findById(otherId).getFriendsIds();
        log.trace("Возвращает общих друзей пользователей с id {} и {}", id, otherId);
        return userFriends.stream()
                .filter(i -> !otherUserFriends.add(i))
                .map(userStorage::findById)
                .toList();
    }
}
