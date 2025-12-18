package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Category;
import br.com.apicomanda.domain.Menu;
import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.dto.menu.CreateMenuRequestDTO;
import br.com.apicomanda.dto.menu.MenuResponseDTO;
import br.com.apicomanda.dto.user.UserResponseDTO;
import br.com.apicomanda.exception.MenuNotFoundException;
import br.com.apicomanda.repository.MenuRepository;
import br.com.apicomanda.service.CategoryService;
import br.com.apicomanda.service.MenuService;
import br.com.apicomanda.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userMenu", allEntries = true),
            @CacheEvict(value = "userCategory", key = "#requestDTO.userId")
    })
    public void createMenu(CreateMenuRequestDTO requestDTO) {
        var userResponse = this.userService.getUserById(requestDTO.userId());
        var category = this.categoryService.getCategory(requestDTO.categoryId());
        var categoryResponse = Category.builder()
                .id(category.id())
                .name(category.name())
                .build();

        var menu = Menu.builder()
                .name(requestDTO.name())
                .description(requestDTO.description())
                .price(requestDTO.price())
                .user(userResponse)
                .category(categoryResponse)
                .build();

        this.menuRepository.save(menu);
    }

    @Override
    @Cacheable(value = "userMenu", key = "#userId + '-' + #categoryId")
    public List<MenuResponseDTO> findAllMenuUserByIdAndCategory(Long userId, Long categoryId) {
        return this.menuRepository.findMenuByUserIdAndCategoryId(userId, categoryId);
    }

    @Override
    @Cacheable(value = "userCategory")
    public List<CategoryResponseDTO> getMenuCategoriesByUserID(Long userId) {
        return this.menuRepository.findCategoriesByUserId(userId);
    }

}
