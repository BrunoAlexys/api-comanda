package br.com.apicomanda.repository;

import br.com.apicomanda.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByAdminIdAndCreatedAtBetween(Long adminId, LocalDateTime start, LocalDateTime end);

    @Query(value = """
                SELECT AVG(EXTRACT(EPOCH FROM (finished_at - created_at)))
                FROM orders
                WHERE admin_id = :adminId
                AND created_at BETWEEN :startOfDay AND :endOfDay
                AND status_order = 'DONE'
                AND finished_at IS NOT NULL
            """, nativeQuery = true)
    Double getAveragePreparationTimeInSeconds(
            @Param("adminId") Long adminId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.items i " +
            "WHERE o.admin.id = :adminId " +
            "AND (:search IS NULL OR :search = '' OR " +
            "CAST(o.id AS string) LIKE %:search% OR " +
            "CAST(o.tableNumber AS string) LIKE %:search% OR " +
            "LOWER(i.menu.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Order> findOrderHistoryWithSearch(
            @Param("adminId") Long adminId,
            @Param("search") String search,
            Pageable pageable
    );
}