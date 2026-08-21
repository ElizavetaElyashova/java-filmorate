package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public Review findById(@PathVariable Long id) {
        return reviewService.findById(id);
    }

    @GetMapping()
    public Collection<Review> findReviews(@RequestParam Long filmId, @RequestParam(defaultValue = "10") int count) {
        return reviewService.findReviews(filmId, count);
    }

    @PostMapping
    public Review create(@RequestBody @Valid Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody @Valid Review newReview) {
        return reviewService.update(newReview);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addReviewLike(@PathVariable long id, @PathVariable long userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addReviewDislike(@PathVariable long id, @PathVariable long userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable long id) {
        reviewService.removeReview(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeReviewLike(@PathVariable long id, @PathVariable long userId) {
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeReviewDislike(@PathVariable long id, @PathVariable long userId) {
        reviewService.removeDislike(id, userId);
    }
}
