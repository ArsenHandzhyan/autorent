package ru.anapa.autorent.service;

import ru.anapa.autorent.model.Category;
import ru.anapa.autorent.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findPopularCategories(int limit) {
        return categoryRepository.findPopularCategories(limit);
    }
}