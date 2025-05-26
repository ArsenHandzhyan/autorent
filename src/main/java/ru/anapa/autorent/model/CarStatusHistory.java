package ru.anapa.autorent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "car_status_history")
public class CarStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarStatus newStatus;

    @Column(nullable = false)
    private LocalDateTime changeDate;

    @Column
    private String changedBy;

    @Column
    private String reason;

    // Конструкторы
    public CarStatusHistory() {
    }

    public CarStatusHistory(Car car, CarStatus oldStatus, CarStatus newStatus, String changedBy) {
        this.car = car;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changeDate = LocalDateTime.now();
        this.changedBy = changedBy;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public CarStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(CarStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public CarStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(CarStatus newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
} 