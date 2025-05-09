package ru.anapa.autorent.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CarListView {
    Long getId();
    String getBrand();
    String getModel();
    Integer getYear();
    String getImageUrl();
    BigDecimal getDailyRate();
    boolean isAvailable();
    String getColor();
    String getTransmission();
    Integer getSeats();
}