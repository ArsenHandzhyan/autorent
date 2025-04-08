package ru.anapa.autorent.service;

import ru.anapa.autorent.model.RentalItem;
import ru.anapa.autorent.repository.RentalItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RentalItemService {

    private final RentalItemRepository rentalItemRepository;

    @Autowired
    public RentalItemService(RentalItemRepository rentalItemRepository) {
        this.rentalItemRepository = rentalItemRepository;
    }

    public List<RentalItem> findPopularItems(int limit) {
        return rentalItemRepository.findPopularItems(limit);
    }

    public List<RentalItem> findNewItems(int limit) {
        return rentalItemRepository.findNewItems(limit);
    }

    public List<RentalItem> findPromotionalItems(int limit) {
        return rentalItemRepository.findPromotionalItems(limit);
    }
}
