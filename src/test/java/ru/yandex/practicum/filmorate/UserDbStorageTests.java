package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {UserDbStorage.class, UserRowMapper.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDbStorageTests {
    private final UserDbStorage userStorage;
    private User user1;
    private User user2;

    @BeforeEach
    void beforeEach() {
        user1 = User.builder()
                .login("qwa")
                .name("qwe")
                .email("qwo@qwu.com")
                .birthday(LocalDate.of(1997, Month.APRIL, 6))
                .build();
        user2 = User.builder()
                .login("ciao")
                .name("giallo")
                .email("cane@gatto.com")
                .birthday(LocalDate.of(1983, Month.DECEMBER, 3))
                .build();
    }

    @Test
    @Order(1)
    public void testUserCreate() {
        long id = userStorage.create(user1).getId();
        assertThat(id).isEqualTo(1);
    }

    @Test
    public void testUserUpdate() {
        long id = userStorage.create(user1).getId();
        user1.setLogin("Updated");
        User updatedUser = userStorage.update(user1);
        assertThat(updatedUser).isEqualTo(userStorage.findById(id));
    }

    @Test
    public void testUserFindById() {
        long id = userStorage.create(user1).getId();
        user1.setId(id);
        assertThat(user1).isEqualTo(userStorage.findById(id));
    }

    @Test
    public void testUserFindAll() {
        user1.setId(userStorage.create(user1).getId());
        user2.setId(userStorage.create(user2).getId());
        assertThat(userStorage.findAll()).contains(user1, user2);
    }

    @Test
    public void testUserRemove() {
        user1.setId(userStorage.create(user1).getId());
        user2.setId(userStorage.create(user2).getId());
        userStorage.remove(user1.getId());
        assertThat(userStorage.findAll()).containsOnly(user2);
    }

    @Test
    public void testUserAddFriend() {
        user1.setId(userStorage.create(user1).getId());
        user2.setId(userStorage.create(user2).getId());
        userStorage.addFriend(user1.getId(), user2.getId());
        assertThat(userStorage.findById(user1.getId()).getFriendsIds()).contains(user2.getId());
        assertThat(userStorage.findById(user2.getId()).getFriendsIds()).isNullOrEmpty();
    }

    @Test
    public void testUserDeleteFriend() {
        user1.setId(userStorage.create(user1).getId());
        user2.setId(userStorage.create(user2).getId());
        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.deleteFriend(user1.getId(), user2.getId());
        assertThat(userStorage.findById(user1.getId()).getFriendsIds()).isNullOrEmpty();
    }

    @Test
    public void testUserFindFriends() {
        user1.setId(userStorage.create(user1).getId());
        user2.setId(userStorage.create(user2).getId());
        userStorage.addFriend(user1.getId(), user2.getId());
        assertThat(userStorage.findFriends(user1.getId())).contains(user2);
    }

} 