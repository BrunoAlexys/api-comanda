package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.domain.User;
import br.com.apicomanda.enums.StatusUser;
import br.com.apicomanda.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDatailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDatailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Deve carregar usuário com sucesso quando e-mail existir e usuário estiver ativo")
    void shouldLoadUserByUsernameSuccessfullyWhenUserIsEnabled() {
        String email = "test@example.com";
        var adminProfile = new Profile(1L, "ROLE_ADMIN");
        var userFromRepo = User.builder()
                .id(1L)
                .email(email)
                .password("encodedPassword")
                .profiles(List.of(adminProfile))
                .status(StatusUser.ENABLED.getStatusValue()) // true
                .build();

        when(userRepository.findByEmail(email)).thenReturn(userFromRepo);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled(), "O usuário deveria estar habilitado (enabled)");
        assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")),
                "O usuário deveria ter a permissão ROLE_ADMIN");

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve carregar usuário, mas marcá-lo como desabilitado, quando status for inativo")
    void shouldLoadUserAsDisabledWhenStatusIsInactive() {
        String email = "disabled@example.com";
        var userFromRepo = User.builder()
                .id(2L)
                .email(email)
                .password("anotherPassword")
                .profiles(List.of()) // Sem perfis
                .status(StatusUser.DISABLED.getStatusValue()) // false
                .build();

        when(userRepository.findByEmail(email)).thenReturn(userFromRepo);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertFalse(userDetails.isEnabled(), "O usuário deveria estar desabilitado (disabled)");

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando o e-mail não existir")
    void shouldThrowUsernameNotFoundExceptionWhenEmailDoesNotExist() {
        String nonExistentEmail = "notfound@example.com";

        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(null);

        var exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistentEmail);
        });

        String expectedErrorMessage = "Usuário com o email: " + nonExistentEmail + " não encontrado.";
        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
    }
}