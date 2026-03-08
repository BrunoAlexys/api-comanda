package br.com.apicomanda.controller;

import br.com.apicomanda.domain.Category;
import br.com.apicomanda.dto.category.CategoryRequestDTO;
import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApplicationConstants.VERSION + "/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize(ApplicationConstants.IS_ADMIN)
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        var category = this.categoryService.createCategory(requestDTO);
        return ResponseEntity.ok(category);
    }

    @GetMapping
    @PreAuthorize(ApplicationConstants.IS_ADMIN)
    public ResponseEntity<List<CategoryResponseDTO>> findAllCategory() {
        var categorieList = this.categoryService.listCategories();
        return ResponseEntity.ok(categorieList);
    }
}
