package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Category;
import br.com.apicomanda.dto.category.CategoryRequestDTO;
import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.exception.CategoryNotFound;
import br.com.apicomanda.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Deve criar uma Categoria com sucesso")
    void shouldCreateCategorySuccessfully() {
        var requestDTO = new CategoryRequestDTO("Bebidas", 1L);
        var expectedCategory = Category.builder()
                .id(1L)
                .name("Bebidas")
                .build();

        when(repository.save(any(Category.class))).thenReturn(expectedCategory);

        Category actualCategory = categoryService.createCategory(requestDTO);

        assertNotNull(actualCategory);
        assertEquals(expectedCategory.getId(), actualCategory.getId());
        assertEquals(expectedCategory.getName(), actualCategory.getName());

        verify(repository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve listar todas as Categorias convertidas para DTO")
    void shouldListCategoriesSuccessfully() {
        var category1 = Category.builder().id(1L).name("Bebidas").build();
        var category2 = Category.builder().id(2L).name("Lanches").build();
        var categoryList = List.of(category1, category2);

        when(repository.findAll()).thenReturn(categoryList);

        List<CategoryResponseDTO> responseList = categoryService.listCategories();

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals(category1.getName(), responseList.get(0).name());
        assertEquals(category2.getName(), responseList.get(1).name());

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar uma Categoria DTO com sucesso quando o ID existir")
    void shouldReturnCategoryDtoWhenIdExists() {
        long categoryId = 1L;
        var category = Category.builder().id(categoryId).name("Sobremesas").build();

        when(repository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponseDTO responseDTO = categoryService.getCategory(categoryId);

        assertNotNull(responseDTO);
        assertEquals(category.getId(), responseDTO.id());
        assertEquals(category.getName(), responseDTO.name());

        verify(repository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("Deve lançar CategoryNotFound quando a Categoria não for encontrada")
    void shouldThrowCategoryNotFoundWhenCategoryDoesNotExist() {
        long nonExistentId = 99L;

        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        var exception = assertThrows(CategoryNotFound.class, () -> {
            categoryService.getCategory(nonExistentId);
        });

        assertEquals("Category not found!", exception.getMessage());

        verify(repository, times(1)).findById(nonExistentId);
    }
}