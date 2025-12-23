package br.com.apicomanda.repository;

import br.com.apicomanda.domain.Menu;
import br.com.apicomanda.dto.category.CategoryResponseDTO;
import br.com.apicomanda.dto.menu.MenuResponseDTO;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query("SELECT DISTINCT new br.com.apicomanda.dto.category.CategoryResponseDTO(c.id, c.name) " +
            "FROM Menu m " +
            "JOIN m.category c " +
            "WHERE m.user.id = :userId")
    List<CategoryResponseDTO> findCategoriesByUserId(@Param("userId") Long userId);
    @Query("SELECT new br.com.apicomanda.dto.menu.MenuResponseDTO(" +
            "m.id, m.name, m.description, m.price, " +
            "new br.com.apicomanda.dto.category.CategoryResponseDTO(c.id, c.name)) " +
            "FROM Menu m JOIN m.category c " +
            "WHERE m.user.id = :userId AND c.id = :categoryId")
    List<MenuResponseDTO> findMenuByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
}
