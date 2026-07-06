package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    Collection<User> findAll();

    User create(@RequestBody @Valid User user);

    User update(@RequestBody @Valid User newUser);

    User findById(Long id);

    List<User> findFriends(Long id);
}
