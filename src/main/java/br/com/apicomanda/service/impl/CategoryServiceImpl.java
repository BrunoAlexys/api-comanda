package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Category;
import br.com.apicomanda.dto.category.CategoryRequestDTO;
import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.exception.CategoryNotFound;
import br.com.apicomanda.repository.CategoryRepository;
import br.com.apicomanda.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;


    @Override
    public Category createCategory(CategoryRequestDTO requestDTO) {
        var category = Category.builder()
                .name(requestDTO.name())
                .build();
        return this.categoryRepository.save(category);
    }

    @Override
    public List<CategoryResponseDTO> listCategories() {
        List<Category> categories = this.categoryRepository.findAll();
        return categories.stream()
                .map(category -> new CategoryResponseDTO(category.getId(), category.getName()))
                .toList();
    }

    @Override
    public CategoryResponseDTO getCategory(Long id) {
        return this.categoryRepository.findById(id)
                .map(category -> new CategoryResponseDTO(category.getId(), category.getName()))
                .orElseThrow(() -> new CategoryNotFound("Category not found!"));
    }
}
