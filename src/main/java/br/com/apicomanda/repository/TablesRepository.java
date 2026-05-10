package br.com.apicomanda.repository;

import br.com.apicomanda.domain.Tables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TablesRepository extends JpaRepository<Tables, Long> {
    List<Tables> findAllByAdminId(Long adminId);
    Optional<Tables> findByNumberTableAndAdminId(Integer numberTable, Long adminId);
}