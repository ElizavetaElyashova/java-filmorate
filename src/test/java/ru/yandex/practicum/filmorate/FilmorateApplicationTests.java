package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmorateApplicationTests {
    private static final String BASE = "http://localhost:8080";
    private static final Type FILMS_LIST_TYPE = new TypeToken<List<Film>>() {}.getType();
    private static HttpClient client;
    private static User basicUser;
    private static Film basicFilm;
    private static Gson gson;

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newBuilder().build();
        gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
    }

    @BeforeEach
    void beforeEach() {
        basicUser = User.builder()
                .login("qwa")
                .name("qwe")
                .email("qwo@qwu.com")
                .birthday(LocalDate.of(1997, Month.APRIL, 6))
                .build();
        basicFilm = Film.builder()
                .name("movie")
                .description("description")
                .releaseDate(LocalDate.of(1895, Month.DECEMBER, 28))
                .duration(100)
                .build();
    }

    private User createUser(String login, String email) throws IOException, InterruptedException {
        User user = User.builder()
                .login(login)
                .name(login)
                .email(email)
                .birthday(LocalDate.of(1997, Month.APRIL, 6))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(user)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, resp.statusCode(), "POST /users должен вернуть 200");

        return gson.fromJson(resp.body(), User.class);
    }

    private Film createFilm(String name) throws IOException, InterruptedException {
        Mpa mpa = new Mpa();
        mpa.setId(1);

        Film film = Film.builder()
                .name(name)
                .description("description")
                .releaseDate(LocalDate.of(1895, Month.DECEMBER, 28))
                .duration(100)
                .mpa(mpa)
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(film)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, resp.statusCode(), "POST /films должен вернуть 200");

        return gson.fromJson(resp.body(), Film.class);
    }

    private void addLike(long filmId, long userId) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/" + filmId + "/like/" + userId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, resp.statusCode(), "PUT /films/{id}/like/{userId} должен вернуть 200");
    }

    @Test
    void postUsers_whenValidUser() throws IOException, InterruptedException {
        String user = gson.toJson(basicUser);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(user))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "POST /users с корректными полями должен вернуть 200");
    }

    @Test
    void postUsers_whenInvalidEmail() throws IOException, InterruptedException {
        basicUser.setEmail("123");
        String user = gson.toJson(basicUser);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(user))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /users с некорректным email должен вернуть 400");
    }

    @Test
    void postUsers_whenInvalidLogin() throws IOException, InterruptedException {
        basicUser.setLogin("qwaqwe\n");
        String user = gson.toJson(basicUser);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(user))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /users с некорректным login должен вернуть 400");
    }

    @Test
    void postUsers_whenEmptyName() throws IOException, InterruptedException {
        basicUser.setName("");
        String user = gson.toJson(basicUser);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(user))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "POST /users с пустым name должен вернуть 200");
        String actualName = resp.body().split(",")[3].split(":")[1];
        actualName = actualName.substring(1, actualName.length() - 1);
        assertEquals(basicUser.getLogin(), actualName, "Если name пустое, должен использоваться login");
    }

    @Test
    void postUsers_whenInvalidBirthday() throws IOException, InterruptedException {
        basicUser.setBirthday(LocalDate.of(2035, Month.APRIL, 7));
        String user = gson.toJson(basicUser);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(user))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /users с birthday в будущем должен вернуть 400");
    }

    @Test
    void postFilms_whenValidFilm() throws IOException, InterruptedException {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        basicFilm.setMpa(mpa);
        String film = gson.toJson(basicFilm);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(film))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "POST /films с корректными полями должен вернуть 200");
    }

    @Test
    void postFilms_whenInvalidName() throws IOException, InterruptedException {
        basicFilm.setName("");
        String film = gson.toJson(basicFilm);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(film))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /films с некорректным name должен вернуть 400");
    }

    @Test
    void postFilms_whenInvalidDescription() throws IOException, InterruptedException {
        basicFilm.setDescription("*".repeat(201));
        String film = gson.toJson(basicFilm);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(film))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /films с некорректным description должен вернуть 400");
    }


    @Test
    void postFilms_whenInvalidReleaseDate() throws IOException, InterruptedException {
        basicFilm.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 27));
        String film = gson.toJson(basicFilm);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(film))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /films с некорректным releaseDate должен вернуть 400");
    }

    @Test
    void postFilms_whenInvalidDuration() throws IOException, InterruptedException {
        basicFilm.setDuration(0);
        String film = gson.toJson(basicFilm);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(film))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /films с некорректным duration должен вернуть 400");
    }

    @Test
    void getCommonFilms_whenCommonFilmsExist_sortedByPopularity() throws IOException, InterruptedException {
        User user1 = createUser("common_u1", "common_u1@mail.com");
        User user2 = createUser("common_u2", "common_u2@mail.com");
        User user3 = createUser("common_u3", "common_u3@mail.com");

        Film film1 = createFilm("common_film1");
        Film film2 = createFilm("common_film2");

        // user1 и user2 лайкнули оба фильма => оба общие
        addLike(film1.getId(), user1.getId());
        addLike(film1.getId(), user2.getId());

        addLike(film2.getId(), user1.getId());
        addLike(film2.getId(), user2.getId());

        // делаем film1 популярнее (добавляем лайк от user3)
        addLike(film1.getId(), user3.getId());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/common?userId=" + user1.getId() + "&friendId=" + user2.getId()))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, resp.statusCode(), "GET /films/common должен вернуть 200");

        List<Film> common = gson.fromJson(resp.body(), FILMS_LIST_TYPE);

        assertEquals(2, common.size(), "Должно вернуться 2 общих фильма");
        assertEquals(film1.getId(), common.get(0).getId(), "Первым должен идти самый популярный фильм (likes desc)");
        assertEquals(film2.getId(), common.get(1).getId(), "Вторым — менее популярный");
    }

    @Test
    void getCommonFilms_whenNoCommonFilms_returnsEmptyList() throws IOException, InterruptedException {
        User user1 = createUser("nocommon_u1", "nocommon_u1@mail.com");
        User user2 = createUser("nocommon_u2", "nocommon_u2@mail.com");

        Film film1 = createFilm("nocommon_film1");
        Film film2 = createFilm("nocommon_film2");

        // Общих лайков нет
        addLike(film1.getId(), user1.getId());
        addLike(film2.getId(), user2.getId());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/common?userId=" + user1.getId() + "&friendId=" + user2.getId()))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, resp.statusCode(), "GET /films/common должен вернуть 200");

        List<Film> common = gson.fromJson(resp.body(), FILMS_LIST_TYPE);
        assertEquals(0, common.size(), "При отсутствии общих фильмов должен вернуться пустой список");
    }
}
