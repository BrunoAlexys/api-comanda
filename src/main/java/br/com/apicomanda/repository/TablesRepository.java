package br.com.apicomanda.repository;

import br.com.apicomanda.domain.Tables;
import br.com.apicomanda.enums.StatusTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TablesRepository extends JpaRepository<Tables, Long> {
    List<Tables> findAllByAdminId(Long adminId);
}
