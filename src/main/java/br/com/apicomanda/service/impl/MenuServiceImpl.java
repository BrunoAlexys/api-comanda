package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Category;
import br.com.apicomanda.domain.Menu;
import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.dto.menu.CreateMenuRequestDTO;
import br.com.apicomanda.dto.menu.MenuResponseDTO;
import br.com.apicomanda.repository.MenuRepository;
import br.com.apicomanda.service.CategoryService;
import br.com.apicomanda.service.MenuService;
import br.com.apicomanda.service.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final AdminService adminService;
    private final CategoryService categoryService;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userMenu", allEntries = true),
            @CacheEvict(value = "userCategory", key = "#requestDTO.adminId()")
    })
    public void createMenu(CreateMenuRequestDTO requestDTO) {
        var userResponse = this.adminService.getAdminById(requestDTO.adminId());
        var category = this.categoryService.getCategory(requestDTO.categoryId());

        var categoryEntity = Category.builder()
                .id(category.id())
                .name(category.name())
                .build();

        var menu = Menu.builder()
                .name(requestDTO.name())
                .description(requestDTO.description())
                .price(requestDTO.price())
                .admin(userResponse)
                .category(categoryEntity)
                .build();

        this.menuRepository.save(menu);
    }

    @Override
    @Cacheable(value = "userMenu", key = "#a0 + '-' + #a1")
    public List<MenuResponseDTO> findAllMenuAdminByIdAndCategory(Long userId, Long categoryId) {
        return this.menuRepository.findMenuByUserIdAndCategoryId(userId, categoryId);
    }

    @Override
    @Cacheable(value = "userCategory", key = "#a0")
    public List<CategoryResponseDTO> getMenuCategoriesByAdminID(Long userId) {
        return this.menuRepository.findCategoriesByUserId(userId);
    }
}