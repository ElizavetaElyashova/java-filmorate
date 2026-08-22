package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.List;

@Repository
@Qualifier("recommendationDbStorage")
@Slf4j
@RequiredArgsConstructor
public class RecommendationDbStorage implements RecommendationStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmMapper;

    @Override
    public List<Film> findRecommendations(Long userId, int topSimilar) {
        String sql = """
            WITH similar_users AS (
                    SELECT t.user_id
                    FROM (
                        SELECT l2.user_id,
                               COUNT(*) AS common_likes,
                               ROW_NUMBER() OVER (ORDER BY COUNT(*) DESC, l2.user_id ASC) AS rn
                        FROM likes l1
                        JOIN likes l2 ON l1.film_id = l2.film_id
                        WHERE l1.user_id = CAST(? AS BIGINT)
                          AND l2.user_id <> CAST(? AS BIGINT)
                        GROUP BY l2.user_id
                    ) t
                    WHERE t.rn <= ?
                )
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.likes,
                       f.rating_id,
                       r.name AS mpa
                FROM likes l
                JOIN similar_users su ON l.user_id = su.user_id   \s
                LEFT JOIN likes my_likes ON my_likes.film_id = l.film_id\s
                                        AND my_likes.user_id = CAST(? AS BIGINT)
                JOIN films f ON f.id = l.film_id
                JOIN ratings r ON f.rating_id = r.id
                WHERE my_likes.film_id IS NULL
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.likes, f.rating_id, r.name
                ORDER BY COUNT(*) DESC, f.id ASC
            """;

        return jdbc.query(sql, filmMapper, userId, userId, topSimilar, userId);
    }
}