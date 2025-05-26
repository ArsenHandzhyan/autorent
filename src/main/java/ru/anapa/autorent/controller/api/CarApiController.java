package ru.anapa.autorent.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.model.CarStatus;
import ru.anapa.autorent.service.CarService;

@RestController
@RequestMapping("/api/cars")
public class CarApiController {

    private final CarService carService;

    public CarApiController(CarService carService) {
        this.carService = carService;
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCarStatus(
            @PathVariable Long id,
            @RequestBody CarStatusUpdateRequest request,
            Authentication authentication) {
        try {
            carService.updateCarStatus(id, request.getStatus(), authentication.getName(), request.getReason());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при обновлении статуса: " + e.getMessage());
        }
    }
}

class CarStatusUpdateRequest {
    private CarStatus status;
    private String reason;

    public CarStatus getStatus() {
        return status;
    }

    public void setStatus(CarStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
} 