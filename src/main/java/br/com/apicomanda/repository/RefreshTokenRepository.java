package br.com.apicomanda.repository;

import br.com.apicomanda.domain.Employee;
import br.com.apicomanda.domain.RefreshToken;
import br.com.apicomanda.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByAdmin(Admin admin);

    void deleteByEmployee(Employee employee);
}
