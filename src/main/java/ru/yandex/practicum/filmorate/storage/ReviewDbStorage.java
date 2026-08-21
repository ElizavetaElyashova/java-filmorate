package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

@Repository
public class ReviewDbStorage {
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ReviewRowMapper reviewMapper;

    private String insertReviewQuery = "INSERT INTO reviews(user_id, film_id, content, is_positive, useful) " +
            "VALUES(?, ?, ?, ?, ?)";
    private String findByIdQuery = "SELECT * FROM reviews WHERE review_id = ?";
    private String updateReviewQuery = "UPDATE reviews SET user_id = ?, film_id = ?, content = ?, is_positive = ?, useful = ? " +
            "WHERE review_id = ?";
    private String findAllQuery = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
    private String findFilmReviewsQuery = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    private String findUsersLikedQuery = "SELECT user_id FROM reviews_likes WHERE review_id = ?";
    private String findUsersDislikedQuery = "SELECT user_id FROM reviews_dislikes WHERE review_id = ?";
    private String insertUserLikedQuery = "INSERT INTO reviews_likes(review_id, user_id) VALUES(?, ?)";
    private String insertUserDislikedQuery = "INSERT INTO reviews_dislikes(review_id, user_id) VALUES(?, ?)";
    private String deleteUserLikedQuery = "DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ?";
    private String deleteUserDislikedQuery = "DELETE FROM reviews_dislikes WHERE review_id = ? AND user_id = ?";
    private String deleteReviewQuery = "DELETE FROM reviews WHERE review_id = ?";

    public Review create(Review review) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertReviewQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, review.getUserId());
            ps.setObject(2, review.getFilmId());
            ps.setObject(3, review.getContent());
            ps.setObject(4, review.getIsPositive());
            ps.setObject(5, 0);
            return ps;
        }, keyHolder);

        review.setReviewId(keyHolder.getKeyAs(Long.class));
        review.setUseful(0);
        return review;
    }

    public Review findById(Long id) {
        try {
            Review review = jdbc.queryForObject(findByIdQuery, reviewMapper, id);
            return review;
        } catch (DataAccessException e) {
            throw new NotFoundException("Отзыв с id = " + id + " не найден.");
        }
    }

    public Collection<Review> findAll(int count) {
        return jdbc.query(findAllQuery, reviewMapper, count);
    }

    public Collection<Review> findFilmReviews(Long filmId, int count) {
        return jdbc.query(findFilmReviewsQuery, reviewMapper, filmId, count);
    }

    public Review update(Review newReview) {
        Review oldReview = findById(newReview.getReviewId());
        if (newReview.getContent() == null) {
            newReview.setContent(oldReview.getContent());
        }
        if (newReview.getUserId() == null) {
            newReview.setUserId(oldReview.getUserId());
        }
        if (newReview.getFilmId() == null) {
            newReview.setFilmId(oldReview.getFilmId());
        }
        if (newReview.getIsPositive() == null) {
            newReview.setIsPositive(oldReview.getIsPositive());
        }
        if (newReview.getUseful() == null) {
            newReview.setUseful(oldReview.getUseful());
        }
        jdbc.update(updateReviewQuery, newReview.getUserId(), newReview.getFilmId(),
                newReview.getContent(), newReview.getIsPositive(), newReview.getUseful(), newReview.getReviewId());
        return newReview;
    }

    public Collection<Long> findUsersLiked(Long reviewId) {
        return jdbc.queryForList(findUsersLikedQuery, Long.class, reviewId);
    }

    public Collection<Long> findUsersDisliked(Long reviewId) {
        return jdbc.queryForList(findUsersDislikedQuery, Long.class, reviewId);
    }

    public void addUserLiked(Long reviewId, Long userId) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertUserLikedQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, reviewId);
            ps.setObject(2, userId);
            return ps;
        }, keyHolder);
    }

    public void addUserDisliked(Long reviewId, Long userId) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertUserDislikedQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, reviewId);
            ps.setObject(2, userId);
            return ps;
        }, keyHolder);
    }

    public void removeUserLiked(Long reviewId, Long userId) {
        jdbc.update(deleteUserLikedQuery, reviewId, userId);
    }

    public void removeUserDisliked(Long reviewId, Long userId) {
        jdbc.update(deleteUserDislikedQuery, reviewId, userId);
    }

    public void removeReview(Long review_id) {
        jdbc.update(deleteReviewQuery, review_id);
    }
}
