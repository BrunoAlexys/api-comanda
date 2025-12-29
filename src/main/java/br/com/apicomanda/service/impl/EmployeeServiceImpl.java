package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Employee;
import br.com.apicomanda.dto.employee.CreateEmployeeDTO;
import br.com.apicomanda.exception.ObjectAlreadyRegisteredException;
import br.com.apicomanda.exception.UserNotFoundException;
import br.com.apicomanda.repository.EmployeeRepository;
import br.com.apicomanda.service.EmployeeService;
import br.com.apicomanda.service.ProfileService;
import br.com.apicomanda.service.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;
    private final AdminService adminService;

    @Override
    @Transactional
    public void createEmployee(CreateEmployeeDTO request) {

        if (this.employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ObjectAlreadyRegisteredException("Não foi possível concluir o cadastro. Verifique seus dados e tente novamente");
        }

        var encryptedPassword = this.passwordEncoder.encode(request.password());
        var profile = this.profileService.findProfile(request.idProfile());
        var user = this.adminService.getAdminById(request.userId());

        var employee = Employee.builder()
                .name(request.name())
                .telephone(request.telephone())
                .email(request.email())
                .password(encryptedPassword)
                .profiles(List.of(profile))
                .admin(user)
                .active(true)
                .build();

        this.employeeRepository.save(employee);
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return this.employeeRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Funcionário não encontrado com o ID: " + id));
    }

    @Override
    public Employee getEmployeeByEmail(String email) {
        var employee = this.employeeRepository.findByEmailIgnoreCase(email);
        if (employee == null) {
            throw new UserNotFoundException("Funcionário não encontrado com o email: " + email);
        }

        return employee;
    }
}
