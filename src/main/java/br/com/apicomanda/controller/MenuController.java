package br.com.apicomanda.controller;

import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.dto.menu.CreateMenuRequestDTO;
import br.com.apicomanda.dto.menu.MenuResponseDTO;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApplicationConstants.VERSION + "/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @PreAuthorize(ApplicationConstants.IS_ADMIN)
    public ResponseEntity<Void> createMenu(@RequestBody @Valid CreateMenuRequestDTO requestDTO) {
        this.menuService.createMenu(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/user/{userId}/categories")
    @PreAuthorize(ApplicationConstants.IS_ADMIN_OR_USER)
    public ResponseEntity<List<CategoryResponseDTO>> getCategoryByUserId(@PathVariable("userId") Long userId) {
        List<CategoryResponseDTO> categories = this.menuService.getMenuCategoriesByAdminID(userId);
        if(categories.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/category/{categoryId}")
    @PreAuthorize(ApplicationConstants.IS_ADMIN_OR_USER)
public ResponseEntity<List<MenuResponseDTO>> getMenuByUserID(@PathVariable("userId") Long userId, @PathVariable("categoryId") Long categoryId) {
        List<MenuResponseDTO> menus = this.menuService.findAllMenuAdminByIdAndCategory(userId, categoryId);
        if (menus.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(menus, HttpStatus.OK);
    }

}
