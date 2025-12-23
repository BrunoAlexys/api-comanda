package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Category;
import br.com.apicomanda.domain.Menu;
import br.com.apicomanda.domain.User;
import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.dto.menu.CreateMenuRequestDTO;
import br.com.apicomanda.dto.menu.MenuResponseDTO;
import br.com.apicomanda.repository.MenuRepository;
import br.com.apicomanda.service.CategoryService;
import br.com.apicomanda.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private MenuServiceImpl menuService;

    @Test
    @DisplayName("Deve criar um Menu com sucesso")
    void shouldCreateMenuSuccessfully() {
        Long userId = 1L;
        Long categoryId = 2L;
        var requestDTO = new CreateMenuRequestDTO("Hamburguer", "Delicioso", BigDecimal.valueOf(25.0), userId, categoryId);

        var user = new User();
        user.setId(userId);

        var categoryDto = new CategoryResponseDTO(categoryId, "Lanches");

        when(userService.getUserById(userId)).thenReturn(user);
        when(categoryService.getCategory(categoryId)).thenReturn(categoryDto);

        menuService.createMenu(requestDTO);

        verify(userService, times(1)).getUserById(userId);
        verify(categoryService, times(1)).getCategory(categoryId);
        verify(menuRepository, times(1)).save(any(Menu.class));
    }

    @Test
    @DisplayName("Deve listar Menus por Usuário e Categoria com sucesso")
    void shouldFindAllMenuUserByIdAndCategory() {
        Long userId = 1L;
        Long categoryId = 2L;

        var categoryDto = new CategoryResponseDTO(categoryId, "Lanches");

        var menuDto1 = new MenuResponseDTO(10L, "X-Bacon", "Com bacon", BigDecimal.valueOf(30.0), categoryDto);
        var menuDto2 = new MenuResponseDTO(20L, "X-Salada", "Com salada", BigDecimal.valueOf(20.0), categoryDto);
        var menuList = List.of(menuDto1, menuDto2);

        when(menuRepository.findMenuByUserIdAndCategoryId(userId, categoryId)).thenReturn(menuList);

        List<MenuResponseDTO> response = menuService.findAllMenuUserByIdAndCategory(userId, categoryId);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(menuDto1.name(), response.get(0).name());
        assertEquals(menuDto2.name(), response.get(1).name());
        assertEquals(categoryDto, response.get(0).category());

        verify(menuRepository, times(1)).findMenuByUserIdAndCategoryId(userId, categoryId);
    }

    @Test
    @DisplayName("Deve retornar categorias de Menu por ID do Usuário")
    void shouldGetMenuCategoriesByUserID() {
        Long userId = 1L;

        var cat1 = new CategoryResponseDTO(1L, "Lanches");
        var cat2 = new CategoryResponseDTO(2L, "Bebidas");
        var categories = List.of(cat1, cat2);

        when(menuRepository.findCategoriesByUserId(userId)).thenReturn(categories);

        List<CategoryResponseDTO> response = menuService.getMenuCategoriesByUserID(userId);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Lanches", response.get(0).name());
        assertEquals("Bebidas", response.get(1).name());

        verify(menuRepository, times(1)).findCategoriesByUserId(userId);
    }
}