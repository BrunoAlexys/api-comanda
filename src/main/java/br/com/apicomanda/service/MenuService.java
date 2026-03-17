package br.com.apicomanda.service;

import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.dto.menu.CreateMenuRequestDTO;
import br.com.apicomanda.dto.menu.MenuResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MenuService {
    void createMenu(CreateMenuRequestDTO requestDTO, MultipartFile file);
    List<MenuResponseDTO> findAllMenuAdminByIdAndCategory(Long userId, Long categoryId);
    List<CategoryResponseDTO> getMenuCategoriesByAdminID(Long userId);
    List<MenuResponseDTO> findAllMenuAdminById(Long userId);
}
