package br.com.apicomanda.repository;

import br.com.apicomanda.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    User findByEmailIgnoreCase(String email);
}
