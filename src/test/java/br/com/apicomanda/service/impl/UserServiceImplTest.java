package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.domain.User;
import br.com.apicomanda.dto.user.CreateUserRequest;
import br.com.apicomanda.dto.user.UserResponseDTO;
import br.com.apicomanda.exception.NotFoundException;
import br.com.apicomanda.exception.ObjectAlreadyRegisteredException;
import br.com.apicomanda.repository.UserRepository;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Test
    @DisplayName("Deve salvar um usuário com sucesso quando o e-mail não existe")
    void shouldSaveUserSuccessfully() {
        var idProfile = 1L;
        var request = new CreateUserRequest("Jhon", "jhon@gmail.com", "88999999999", "Teste@123", idProfile);
        var profile = new Profile(idProfile, "ROLE_USER");
        var encryptedPassword = "encrypted_password";

        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn(encryptedPassword);
        when(profileService.findProfile(request.idProfile())).thenReturn(profile);

        userService.saveUser(request);

        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();

        assertEquals(request.name(), savedUser.getName());
        assertEquals(request.email(), savedUser.getEmail());
        assertEquals(encryptedPassword, savedUser.getPassword());
        assertNotNull(savedUser.getProfiles());
        assertEquals(1, savedUser.getProfiles().size());
        assertEquals(profile, savedUser.getProfiles().get(0));

        verify(userRepository, times(1)).existsByEmailIgnoreCase(request.email());
        verify(passwordEncoder, times(1)).encode(request.password());
        verify(profileService, times(1)).findProfile(request.idProfile());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar um usuário com e-mail que já existe")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        var idProfile = 1L;
        var request = new CreateUserRequest("Jhon", "jhon@gmail.com", "88999999999", "Teste@123", idProfile);

        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        var exception = assertThrows(ObjectAlreadyRegisteredException.class, () -> {
            userService.saveUser(request);
        });

        assertEquals("Não foi possível concluir o cadastro. Verifique seus dados e tente novamente", exception.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
        verify(profileService, never()).findProfile(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve encontrar um usuário por e-mail e retornar um DTO")
    void shouldFindByEmailAndReturnDTO() {
        var email = "jhon@gmail.com";
        var user = User.builder().email(email).name("Jhon").profiles(List.of()).build();

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(user);

        UserResponseDTO result = userService.findByEmail(email);

        assertNotNull(result);
        assertEquals(user.getName(), result.name());
        assertEquals(user.getEmail(), result.email());

        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar por e-mail um usuário que não existe")
    void shouldThrowExceptionWhenFindingByNonExistentEmail() {
        var email = "nonexistent@gmail.com";

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            userService.findByEmail(email);
        });

        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("Deve encontrar um usuário por ID com sucesso")
    void shouldFindUserByIdSuccessfully() {
        var id = 1L;
        var user = User.builder().id(id).email("test@test.com").build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());

        verify(userRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar NotFounException quando o ID do usuário não for encontrado")
    void shouldThrowNotFoundExceptionForNonExistentUserId() {
        var id = 99L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            userService.getUserById(id);
        });

        verify(userRepository, times(1)).findById(id);
    }
}