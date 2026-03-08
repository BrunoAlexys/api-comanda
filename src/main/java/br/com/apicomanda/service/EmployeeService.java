package br.com.apicomanda.service;

import br.com.apicomanda.domain.Employee;
import br.com.apicomanda.dto.employee.CreateEmployeeDTO;

public interface EmployeeService {
    void createEmployee(CreateEmployeeDTO request);
    Employee getEmployeeById(Long id);

    Employee getEmployeeByEmail(String email);
}
