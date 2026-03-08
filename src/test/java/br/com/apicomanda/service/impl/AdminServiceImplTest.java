package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Admin;
import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.dto.admin.AdminResponseDTO;
import br.com.apicomanda.dto.admin.CreateAdminRequest;
import br.com.apicomanda.exception.NotFoundException;
import br.com.apicomanda.exception.ObjectAlreadyRegisteredException;
import br.com.apicomanda.exception.UserNotFoundException;
import br.com.apicomanda.repository.AdminRepository;
import br.com.apicomanda.repository.EmployeeRepository;
import br.com.apicomanda.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminServiceImpl userService;

    @Captor
    private ArgumentCaptor<Admin> userArgumentCaptor;

    @Test
    @DisplayName("Deve salvar um usuário com sucesso quando o e-mail não existe")
    void shouldSaveUserSuccessfully() {
        var idProfile = 1L;
        var request = new CreateAdminRequest("Jhon", "jhon@gmail.com", "88999999999", "Teste@123", idProfile);
        var profile = new Profile(idProfile, "ROLE_USER");
        var encryptedPassword = "encrypted_password";

        when(adminRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn(encryptedPassword);
        when(profileService.findProfile(request.idProfile())).thenReturn(profile);

        userService.saveUser(request);

        verify(adminRepository, times(1)).save(userArgumentCaptor.capture());

        Admin savedAdmin = userArgumentCaptor.getValue();

        assertEquals(request.name(), savedAdmin.getName());
        assertEquals(request.email(), savedAdmin.getEmail());
        assertEquals(encryptedPassword, savedAdmin.getPassword());
        assertNotNull(savedAdmin.getProfiles());
        assertEquals(1, savedAdmin.getProfiles().size());
        assertEquals(profile, savedAdmin.getProfiles().get(0));

        verify(adminRepository, times(1)).existsByEmailIgnoreCase(request.email());
        verify(passwordEncoder, times(1)).encode(request.password());
        verify(profileService, times(1)).findProfile(request.idProfile());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar um usuário com e-mail que já existe")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        var idProfile = 1L;
        var request = new CreateAdminRequest("Jhon", "jhon@gmail.com", "88999999999", "Teste@123", idProfile);

        when(adminRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        var exception = assertThrows(ObjectAlreadyRegisteredException.class, () -> {
            userService.saveUser(request);
        });

        assertEquals("Não foi possível concluir o cadastro. Verifique seus dados e tente novamente", exception.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
        verify(profileService, never()).findProfile(anyLong());
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    @DisplayName("Deve encontrar um usuário por e-mail e retornar um DTO")
    void shouldFindByEmailAndReturnDTO() {
        var email = "jhon@gmail.com";
        var user = Admin.builder().email(email).name("Jhon").profiles(List.of()).build();

        when(adminRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        AdminResponseDTO result = userService.findByEmail(email);

        assertNotNull(result);
        assertEquals(user.getName(), result.name());
        assertEquals(user.getEmail(), result.email());

        verify(adminRepository, times(1)).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar por e-mail um usuário que não existe")
    void shouldThrowExceptionWhenFindingByNonExistentEmail() {
        var email = "nonexistent@gmail.com";

        when(adminRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            userService.findByEmail(email);
        });

        verify(adminRepository, times(1)).findByEmailIgnoreCase(email);
        verify(employeeRepository, times(1)).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("Deve encontrar um usuário por ID com sucesso")
    void shouldFindUserByIdSuccessfully() {
        var id = 1L;
        var user = Admin.builder().id(id).email("test@test.com").build();

        when(adminRepository.findById(id)).thenReturn(Optional.of(user));

        Admin result = userService.getAdminById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());

        verify(adminRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar NotFounException quando o ID do usuário não for encontrado")
    void shouldThrowNotFoundExceptionForNonExistentUserId() {
        var id = 99L;

        when(adminRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            userService.getAdminById(id);
        });

        verify(adminRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve retornar a entidade User quando buscar por e-mail existente")
    void shouldReturnUserEntityWhenEmailExists() {
        var email = "entity@gmail.com";
        var expectedUser = Admin.builder().id(1L).email(email).name("Entity User").build();

        when(adminRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(expectedUser));

        Admin actualAdmin = userService.getAdminByEmail(email);

        assertNotNull(actualAdmin);
        assertEquals(expectedUser, actualAdmin);
        assertEquals(expectedUser.getEmail(), actualAdmin.getEmail());

        verify(adminRepository, times(1)).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("Deve lançar exceção quando buscar entidade Admin por e-mail inexistente")
    void shouldThrowExceptionWhenGettingAdminByNonExistentEmail() {
        var email = "notfound@gmail.com";
        when(adminRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.getAdminByEmail(email);
        });

        verify(adminRepository, times(1)).findByEmailIgnoreCase(email);
    }
}