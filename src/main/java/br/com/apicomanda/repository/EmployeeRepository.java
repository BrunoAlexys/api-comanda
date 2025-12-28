package br.com.apicomanda.repository;

import br.com.apicomanda.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Employee findByEmailIgnoreCase(String email);
}
