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
    private String findUsersReactedQuery = "SELECT user_id FROM reviews_reactions WHERE review_id = ? AND user_liked = ?";
    private String insertUserReactedQuery = "INSERT INTO reviews_reactions(review_id, user_id, user_liked) VALUES(?, ?, ?)";
    private String deleteUserReactedQuery = "DELETE FROM reviews_reactions WHERE review_id = ? AND user_id = ? AND user_liked = ?";
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

    public Collection<Long> findUsersReacted(Long reviewId, boolean userLiked) {
        return jdbc.queryForList(findUsersReactedQuery, Long.class, reviewId, userLiked);
    }

    public void addUserReaction(Long reviewId, Long userId, boolean userLiked) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertUserReactedQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, reviewId);
            ps.setObject(2, userId);
            ps.setObject(3, userLiked);
            return ps;
        }, keyHolder);
    }

    public void removeUserReacted(Long reviewId, Long userId, boolean userLiked) {
        jdbc.update(deleteUserReactedQuery, reviewId, userId, userLiked);
    }

    public void removeReview(Long reviewId) {
        jdbc.update(deleteReviewQuery, reviewId);
    }
}
