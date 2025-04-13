package ru.anapa.autorent.dto;

import lombok.Data;
import ru.anapa.autorent.model.Review;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private String userName;
    private Integer rating;
    private String reviewText;
    private Long rentalId;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserName(review.getUserName());
        response.setRating(review.getRating());
        response.setReviewText(review.getReviewText());
        response.setRentalId(review.getRentalId());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}