package org.pgno20.medimart.controller;

import jakarta.validation.Valid;
import org.pgno20.medimart.dto.CategoryCreateRequest;
import org.pgno20.medimart.dto.CategoryResponse;
import org.pgno20.medimart.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest req) {
        return ResponseEntity.ok(categoryService.create(req));
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(categoryService.listAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryCreateRequest req) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}