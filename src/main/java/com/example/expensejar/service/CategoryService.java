package com.example.expensejar.service;

import com.example.expensejar.dto.CategoryRequest;
import com.example.expensejar.entity.Category;
import com.example.expensejar.exception.ResourceNotFoundException;
import com.example.expensejar.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }
        Category category = new Category(request.getName().trim());
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    public Category updateCategory(Long id, CategoryRequest request) {
        Category category = getCategoryById(id);
        String newName = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(newName) && categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new IllegalArgumentException("Category with name '" + newName + "' already exists");
        }
        category.setName(newName);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}
