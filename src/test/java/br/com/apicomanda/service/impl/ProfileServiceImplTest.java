package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.exception.NotFounException;
import br.com.apicomanda.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private ProfileRepository repository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    @DisplayName("Deve retornar um Perfil com sucesso quando o ID existir")
    void shouldReturnProfileWhenIdExists() {
        long profileId = 1L;
        var expectedProfile = new Profile(profileId, "ROLE_ADMIN");

        when(repository.findById(profileId)).thenReturn(Optional.of(expectedProfile));

        Profile actualProfile = profileService.findProfile(profileId);

        assertNotNull(actualProfile);
        assertEquals(expectedProfile.getId(), actualProfile.getId());
        assertEquals(expectedProfile.getName(), actualProfile.getName());

        verify(repository, times(1)).findById(profileId);
    }

    @Test
    @DisplayName("Deve lançar NotFounException quando o Perfil não for encontrado")
    void shouldThrowNotFoundExceptionWhenProfileDoesNotExist() {
        long nonExistentId = 99L;

        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFounException.class, () -> {
            profileService.findProfile(nonExistentId);
        });

        assertEquals("Perfil não encontrado", exception.getMessage());

        verify(repository, times(1)).findById(nonExistentId);
    }
}