package org.pgno20.medimart.service;

import org.pgno20.medimart.dto.CategoryCreateRequest;
import org.pgno20.medimart.dto.CategoryResponse;
import org.pgno20.medimart.model.Category;
import org.pgno20.medimart.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

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
        return categoryRepository.findAll(pageable).map(this::toResponse);
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setStatus(category.getStatus());
        return response;
    }
}
