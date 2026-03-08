package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Category;
import br.com.apicomanda.dto.category.CategoryRequestDTO;
import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.exception.CategoryNotFound;
import br.com.apicomanda.repository.AdminRepository;
import br.com.apicomanda.repository.CategoryRepository;
import br.com.apicomanda.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final AdminRepository adminRepository;

    @Override
    @Transactional
    @CacheEvict(value = "categoriesList", allEntries = true)
    public Category createCategory(CategoryRequestDTO requestDTO) {
        var admin = this.adminRepository.findById(requestDTO.adminId())
                .orElseThrow(() -> new UsernameNotFoundException("Admin não encontrado!"));
        var category = Category.builder()
                .name(requestDTO.name())
                .admin(admin)
                .build();
        return this.categoryRepository.save(category);
    }

    @Override
    @Cacheable(value = "categoriesList")
    public List<CategoryResponseDTO> listCategories() {
        List<Category> categories = this.categoryRepository.findAll();
        return categories.stream()
                .map(category -> new CategoryResponseDTO(category.getId(), category.getName()))
                .toList();
    }

    @Override
    @Cacheable(value = "category", key = "#a0")
    public CategoryResponseDTO getCategory(Long id) {
        return this.categoryRepository.findById(id)
                .map(category -> new CategoryResponseDTO(category.getId(), category.getName()))
                .orElseThrow(() -> new CategoryNotFound("Category not found!"));
    }
}
