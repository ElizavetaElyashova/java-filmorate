package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.util.Collection;

@Service
public class ReviewService {
    @Autowired
    private ReviewDbStorage reviewDbStorage;
    private FilmDbStorage filmDbStorage;
    private UserDbStorage userDbStorage;

    @Autowired
    public ReviewService(FilmDbStorage filmDbStorage, UserDbStorage userDbStorage) {
        this.filmDbStorage = filmDbStorage;
        this.userDbStorage = userDbStorage;
    }

    public Review findById(long id) {
        return reviewDbStorage.findById(id);
    }

    public Review create(Review review) {
        filmDbStorage.findById(review.getFilmId());
        userDbStorage.findById(review.getUserId());
        return reviewDbStorage.create(review);
    }

    public Review update(Review newReview) {
        if (newReview.getFilmId() != null) {
            filmDbStorage.findById(newReview.getFilmId());
        }
        if (newReview.getUserId() != null) {
            userDbStorage.findById(newReview.getUserId());
        }
        return reviewDbStorage.update(newReview);
    }

    public Collection<Review> findReviews(Long filmId, int count) {
        if (filmId == null) {
            return reviewDbStorage.findAll(count);
        } else {
            filmDbStorage.findById(filmId);
            return reviewDbStorage.findFilmReviews(filmId, count);
        }
    }

    public void addLike(Long reviewId, Long userId) {
        Review review = reviewDbStorage.findById(reviewId);
        userDbStorage.findById(userId);
        Collection<Long> usersLiked = reviewDbStorage.findUsersLiked(reviewId);
        if (!usersLiked.contains(userId)) {
            review = removeDislike(reviewId, userId);
            review.setUseful(review.getUseful() + 1);
            update(review);
            reviewDbStorage.addUserLiked(reviewId, userId);
        }
    }

    public void addDislike(Long reviewId, Long userId) {
        Review review = reviewDbStorage.findById(reviewId);
        userDbStorage.findById(userId);
        Collection<Long> usersDisliked = reviewDbStorage.findUsersDisliked(reviewId);
        if (!usersDisliked.contains(userId)) {
            review = removeLike(reviewId, userId);
            review.setUseful(review.getUseful() - 1);
            update(review);
            reviewDbStorage.addUserDisliked(reviewId, userId);
        }
    }

    public Review removeLike(Long reviewId, Long userId) {
        Review review = reviewDbStorage.findById(reviewId);
        userDbStorage.findById(userId);
        Collection<Long> usersLiked = reviewDbStorage.findUsersLiked(reviewId);
        if (usersLiked.contains(userId)) {
            review.setUseful(review.getUseful() - 1);
            review = update(review);
            reviewDbStorage.removeUserLiked(reviewId, userId);
        }
        return review;
    }

    public Review removeDislike(Long reviewId, Long userId) {
        Review review = reviewDbStorage.findById(reviewId);
        userDbStorage.findById(userId);
        Collection<Long> usersDisliked = reviewDbStorage.findUsersDisliked(reviewId);
        if (usersDisliked.contains(userId)) {
            review.setUseful(review.getUseful() + 1);
            review = update(review);
            reviewDbStorage.removeUserDisliked(reviewId, userId);
        }
        return review;
    }

    public void removeReview(Long id) {
        reviewDbStorage.removeReview(id);
    }
}
