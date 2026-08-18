package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmorateApplicationTests {
    private static final String BASE = "http://localhost:8080";
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

}
