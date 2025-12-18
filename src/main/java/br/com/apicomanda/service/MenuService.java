package br.com.apicomanda.service;

import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.dto.menu.CreateMenuRequestDTO;
import br.com.apicomanda.dto.menu.MenuResponseDTO;

import java.util.List;

public interface MenuService {
    void createMenu(CreateMenuRequestDTO requestDTO);
    List<MenuResponseDTO> findAllMenuUserByIdAndCategory(Long userId, Long categoryId);
    List<CategoryResponseDTO> getMenuCategoriesByUserID(Long userId);
}
