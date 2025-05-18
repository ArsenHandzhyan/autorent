package ru.anapa.autorent.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.dto.ReviewCreateRequest;
import ru.anapa.autorent.dto.ReviewResponse;
import ru.anapa.autorent.service.ReviewService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Отображает форму для создания отзыва
     */
    @GetMapping("/create")
    public ResponseEntity<?> showCreateReviewForm(@RequestParam Long rentalId, Model model) {
        try {
            ReviewCreateRequest reviewRequest = new ReviewCreateRequest();
            reviewRequest.setRentalId(rentalId);
            return ResponseEntity.ok(Map.of(
                "reviewRequest", reviewRequest,
                "rentalId", rentalId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при создании формы отзыва"));
        }
    }

    /**
     * Обрабатывает отправку формы создания отзыва (HTML форма)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createReview(@Valid @ModelAttribute("reviewRequest") ReviewCreateRequest request,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ошибка валидации данных отзыва"));
        }

        try {
            ReviewResponse createdReview = reviewService.createReview(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "message", "Отзыв успешно создан",
                        "review", createdReview
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при создании отзыва"));
        }
    }

    /**
     * API метод для создания отзыва (AJAX запрос)
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createReviewApi(@Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение всех отзывов для конкретного проката
     */
    @GetMapping("/rental/{rentalId}")
    public String getReviewsByRentalId(@PathVariable Long rentalId, Model model) {
        List<ReviewResponse> reviews = reviewService.getReviewsByRentalId(rentalId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("rentalId", rentalId);
        return "reviews/list";
    }

    /**
     * API метод для получения отзывов по ID проката
     */
    @GetMapping("/api/rental/{rentalId}")
    @ResponseBody
    public ResponseEntity<List<ReviewResponse>> getReviewsByRentalIdApi(@PathVariable Long rentalId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByRentalId(rentalId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Удаление отзыва (только для администраторов)
     */
    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, @RequestParam Long rentalId) {
        reviewService.deleteReview(id);
        return "redirect:/reviews/rental/" + rentalId;
    }

    /**
     * API метод для удаления отзыва
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteReviewApi(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}