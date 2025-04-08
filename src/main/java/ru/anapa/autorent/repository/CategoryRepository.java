package ru.anapa.autorent.repository;


import ru.anapa.autorent.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = "SELECT c FROM Category c LEFT JOIN c.rentalItems ri GROUP BY c.id ORDER BY COUNT(ri) DESC")
    List<Category> findPopularCategories(@Param("limit") int limit);
}
