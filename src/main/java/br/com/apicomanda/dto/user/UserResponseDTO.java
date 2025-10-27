package br.com.apicomanda.dto.user;

import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.domain.User;

import java.io.Serializable;
import java.util.List;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        String telephone,
        List<String> profiles
) {
    public UserResponseDTO(User user) {
        this(user.getId(), user.getName(), user.getEmail(), user.getTelephone(),
                user.getProfiles().stream()
                        .map(Profile::getName)
                        .toList());
    }
}