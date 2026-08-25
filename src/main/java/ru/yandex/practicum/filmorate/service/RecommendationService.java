package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.RecommendationStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
public class RecommendationService {
    //количество пользователей, по которым будет происходить подбор рекомендаций
    private static final int DEFAULT_COUNT_TOP_SIMILAR = 3;

    private final RecommendationStorage recommendationStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreDbStorage;

    public RecommendationService(
            @Qualifier("recommendationDbStorage") RecommendationStorage recommendationStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            GenreDbStorage genreDbStorage
    ) {
        this.recommendationStorage = recommendationStorage;
        this.userStorage = userStorage;
        this.genreDbStorage = genreDbStorage;
    }

    public List<Film> getRecommendations(Long userId) {
        userStorage.findById(userId);

        List<Film> films = recommendationStorage.findRecommendations(userId, DEFAULT_COUNT_TOP_SIMILAR);

        for (Film film : films) {
            film.setGenres(genreDbStorage.findFilmGenres(film.getId()));
        }

        log.trace("Recommendations for userId={}: {}", userId, films.size());
        return films;
    }
}