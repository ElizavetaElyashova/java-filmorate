package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FeedDbStorage;
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
    private FeedDbStorage feedDbStorage;

    @Autowired
    public ReviewService(FilmDbStorage filmDbStorage, UserDbStorage userDbStorage, FeedDbStorage feedDbStorage) {
        this.filmDbStorage = filmDbStorage;
        this.userDbStorage = userDbStorage;
        this.feedDbStorage = feedDbStorage;
    }

    public Review findById(long id) {
        return reviewDbStorage.findById(id);
    }

    public Review create(Review review) {
        filmDbStorage.findById(review.getFilmId());
        userDbStorage.findById(review.getUserId());
        review = reviewDbStorage.create(review);
        feedDbStorage.create(Event.builder()
                        .userId(review.getUserId())
                        .entityId(review.getReviewId())
                        .build(),
                2, 2
        );
        return review;
    }

    public Review update(Review newReview) {
        if (newReview.getFilmId() != null) {
            filmDbStorage.findById(newReview.getFilmId());
        }
        if (newReview.getUserId() != null) {
            userDbStorage.findById(newReview.getUserId());
        }
        newReview = reviewDbStorage.update(newReview);
        feedDbStorage.create(Event.builder()
                .userId(newReview.getUserId())
                .entityId(newReview.getReviewId())

                .build(), 2, 3);
        return newReview;
    }

    public Collection<Review> findReviews(Long filmId, int count) {
        if (filmId == null) {
            return reviewDbStorage.findAll(count);
        } else {
            filmDbStorage.findById(filmId);
            return reviewDbStorage.findFilmReviews(filmId, count);
        }
    }

    public void addReaction(Long reviewId, Long userId, boolean userLiked) {
        Review review = reviewDbStorage.findById(reviewId);
        userDbStorage.findById(userId);
        Collection<Long> usersLiked = reviewDbStorage.findUsersReacted(reviewId, userLiked);
        if (!usersLiked.contains(userId)) {
            review = removeReaction(reviewId, userId, !userLiked);
            int i = userLiked ? 1 : -1;
            review.setUseful(review.getUseful() + i);
            update(review);
            reviewDbStorage.addUserReaction(reviewId, userId, userLiked);
        }
    }


    public Review removeReaction(Long reviewId, Long userId, boolean userLiked) {
        Review review = reviewDbStorage.findById(reviewId);
        userDbStorage.findById(userId);
        Collection<Long> usersReacted = reviewDbStorage.findUsersReacted(reviewId, userLiked);
        if (usersReacted.contains(userId)) {
            int i = userLiked ? 1 : -1;
            review.setUseful(review.getUseful() - i);
            review = update(review);
            reviewDbStorage.removeUserReacted(reviewId, userId, userLiked);
        }
        return review;
    }

    public void removeReview(Long id) {
        Review review = reviewDbStorage.findById(id);
        reviewDbStorage.removeReview(id);
        feedDbStorage.create(Event.builder()
                .userId(review.getUserId())
                .entityId(id)
                .build(), 2, 1);
    }
}
