package org.pgno20.medimart.service;

import org.pgno20.medimart.dto.CategoryCreateRequest;
import org.pgno20.medimart.dto.CategoryResponse;
import org.pgno20.medimart.model.Category;
import org.pgno20.medimart.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse create(CategoryCreateRequest req) {
        try {
            Category category = new Category();
            category.setName(req.getName());
            category.setStatus("ACTIVE");
            Category saved = categoryRepository.save(category);
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("A category with this name already exists.");
        }
    }

    public Page<CategoryResponse> listAll(Pageable pageable) {
        return categoryRepository.findByStatus("ACTIVE", pageable).map(this::toResponse);
    }

    public CategoryResponse update(Long id, CategoryCreateRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        try {
            category.setName(req.getName());
            Category saved = categoryRepository.save(category);
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("A category with this name already exists.");
        }
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        category.setStatus("INACTIVE");
        categoryRepository.save(category);
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setStatus(category.getStatus());
        return response;
    }
}
