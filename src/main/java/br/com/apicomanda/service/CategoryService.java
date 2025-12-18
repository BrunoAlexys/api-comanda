package br.com.apicomanda.service;

import br.com.apicomanda.domain.Category;
import br.com.apicomanda.dto.category.CategoryRequestDTO;
import br.com.apicomanda.dto.category.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    Category createCategory(CategoryRequestDTO requestDTO);
    List<CategoryResponseDTO> listCategories();
    CategoryResponseDTO getCategory(Long id);
}
