package ru.anapa.autorent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rental_items")
@Data
public class RentalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "price_per_day")
    private BigDecimal pricePerDay;

    private BigDecimal deposit;

    @Column(name = "available_quantity")
    private Integer availableQuantity;

    private String image;

    @Column(name = "is_promotional")
    private Boolean isPromotional = false;

    // Здесь проблема - убедитесь, что mappedBy указывает на правильное имя поля в Rental
    @OneToMany(mappedBy = "rentalItem")
    private List<Rental> rentals;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}