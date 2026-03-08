package br.com.apicomanda.repository;

import br.com.apicomanda.domain.Fee;
import br.com.apicomanda.dto.fee.FeeResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee,Long> {
    List<Fee> findByAdminId(Long adminId);
}
