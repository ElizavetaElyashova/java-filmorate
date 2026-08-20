package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    @Getter
    @Autowired
    @Qualifier("userDbStorage")
    private UserStorage userStorage;
    private FeedDbStorage feedDbStorage;

    @Autowired
    public UserService(FeedDbStorage feedDbStorage) {
        this.feedDbStorage = feedDbStorage;
    }

    public void addFriend(Long id, Long friendId) {
        userStorage.addFriend(id, friendId);
        feedDbStorage.create(Event.builder()
                .userId(id)
                .entityId(friendId)
                .eventType(new EventType(3))
                .operation(new Operation(2))
                .build());
        log.trace("Пользователь с id = {} добавляет в друзья пользователя с id = {}", id, friendId);
    }

    public void deleteFriend(Long id, Long friendId) {
        userStorage.deleteFriend(id, friendId);
        feedDbStorage.create(Event.builder()
                .userId(id)
                .entityId(friendId)
                .eventType(new EventType(3))
                .operation(new Operation(1))
                .build());
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

    public Collection<Event> findFeedByUserId(long id) {
        return feedDbStorage.findFeedByUserId(id);
    }
}
