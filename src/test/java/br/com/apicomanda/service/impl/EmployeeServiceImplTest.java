package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Admin;
import br.com.apicomanda.domain.Employee;
import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.dto.employee.CreateEmployeeDTO;
import br.com.apicomanda.exception.ObjectAlreadyRegisteredException;
import br.com.apicomanda.exception.UserNotFoundException;
import br.com.apicomanda.repository.EmployeeRepository;
import br.com.apicomanda.service.AdminService;
import br.com.apicomanda.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfileService profileService;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private CreateEmployeeDTO createDTO;
    private Employee employee;
    private Admin admin;
    private Profile profile;

    @BeforeEach
    void setUp() {
        createDTO = new CreateEmployeeDTO("João Silva", "11999999999", "joao@email.com", "senha123", 1L, 1L);

        admin = new Admin();
        admin.setId(1L);

        profile = new Profile(1L, "ROLE_EMPLOYEE");

        employee = Employee.builder()
                .id(10L)
                .name(createDTO.name())
                .email(createDTO.email())
                .admin(admin)
                .build();
    }

    @Test
    @DisplayName("Deve criar um funcionário com sucesso")
    void shouldCreateEmployeeSuccessfully() {
        when(employeeRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("password_encrypted");
        when(profileService.findProfile(anyLong())).thenReturn(profile);
        when(adminService.getAdminById(anyLong())).thenReturn(admin);

        employeeService.createEmployee(createDTO);

        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(passwordEncoder, times(1)).encode(createDTO.password());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o email já estiver cadastrado")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(employeeRepository.existsByEmailIgnoreCase(createDTO.email())).thenReturn(true);

        assertThrows(ObjectAlreadyRegisteredException.class, () -> {
            employeeService.createEmployee(createDTO);
        });

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar funcionário ao buscar por ID existente")
    void shouldReturnEmployeeWhenIdExists() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("joao@email.com", result.getEmail());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar por ID inexistente")
    void shouldThrowExceptionWhenIdDoesNotExist() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            employeeService.getEmployeeById(1L);
        });

        assertTrue(exception.getMessage().contains("Funcionário não encontrado com o ID: 1"));
    }

    @Test
    @DisplayName("Deve retornar funcionário ao buscar por e-mail existente")
    void shouldReturnEmployeeWhenEmailExists() {
        when(employeeRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeByEmail("joao@email.com");

        assertNotNull(result);
        assertEquals("joao@email.com", result.getEmail());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar por e-mail inexistente")
    void shouldThrowExceptionWhenEmailDoesNotExist() {
        when(employeeRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            employeeService.getEmployeeByEmail("invalido@email.com");
        });
    }
}