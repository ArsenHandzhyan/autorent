package ru.anapa.autorent.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "car_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "description")
    @Builder.Default
    private String description = "";

    @Column(name = "is_main")
    @Builder.Default
    private boolean isMain = false;

    @Column(name = "display_order")
    @Builder.Default
    private int displayOrder = 0;

    @Column(name = "rotation")
    private Integer rotation = 0;

    public Integer getRotation() {
        return rotation;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }
} 