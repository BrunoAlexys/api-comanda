package br.com.apicomanda.service;

import br.com.apicomanda.domain.User;
import br.com.apicomanda.dto.user.CreateUserRequest;
import br.com.apicomanda.dto.user.UserResponseDTO;

import java.util.Optional;

public interface UserService {
    void saveUser(CreateUserRequest userRequest);
    UserResponseDTO findByEmail(String email);
    User getUserByEmail(String email);
    User getUserById(Long id);
}
