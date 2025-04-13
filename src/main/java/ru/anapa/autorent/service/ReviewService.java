package ru.anapa.autorent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.anapa.autorent.dto.ReviewCreateRequest;
import ru.anapa.autorent.dto.ReviewResponse;
import ru.anapa.autorent.model.Review;
import ru.anapa.autorent.repository.ReviewRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewResponse createReview(ReviewCreateRequest request) {
        Review review = new Review();
        review.setUserName(request.getUserName());
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review.setRentalId(request.getRentalId());

        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.fromEntity(savedReview);
    }

    public List<ReviewResponse> getReviewsByRentalId(Long rentalId) {
        return reviewRepository.findByRentalIdOrderByCreatedAtDesc(rentalId)
                .stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв с ID " + id + " не найден"));
        return ReviewResponse.fromEntity(review);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}