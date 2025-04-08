package ru.anapa.autorent.repository;

import ru.anapa.autorent.model.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalItemRepository extends JpaRepository<RentalItem, Long> {

    @Query(value = "SELECT ri FROM RentalItem ri WHERE ri.availableQuantity > 0 " +
            "ORDER BY SIZE(ri.rentals) DESC")
    List<RentalItem> findPopularItems(@Param("limit") int limit);

    @Query(value = "SELECT ri FROM RentalItem ri WHERE ri.availableQuantity > 0 " +
            "ORDER BY ri.createdAt DESC")
    List<RentalItem> findNewItems(@Param("limit") int limit);

    @Query(value = "SELECT ri FROM RentalItem ri WHERE ri.availableQuantity > 0 " +
            "AND ri.isPromotional = true")
    List<RentalItem> findPromotionalItems(@Param("limit") int limit);
}
