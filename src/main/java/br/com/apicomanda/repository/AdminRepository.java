package br.com.apicomanda.repository;

import br.com.apicomanda.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    Admin findByEmailIgnoreCase(String email);
}
