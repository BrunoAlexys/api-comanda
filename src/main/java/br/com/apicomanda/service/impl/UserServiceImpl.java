package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.User;
import br.com.apicomanda.dto.user.CreateUserRequest;
import br.com.apicomanda.dto.user.UserResponseDTO;
import br.com.apicomanda.exception.NotFounException;
import br.com.apicomanda.exception.ObjectAlreadyRegisteredException;
import br.com.apicomanda.repository.UserRepository;
import br.com.apicomanda.service.ProfileService;
import br.com.apicomanda.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void saveUser(CreateUserRequest userRequest) {
        if (this.userRepository.existsByEmailIgnoreCase(userRequest.email())) {
            throw new ObjectAlreadyRegisteredException("Não foi possível concluir o cadastro. Verifique seus dados e tente novamente");
        }

        var encryptedPassword = passwordEncoder.encode(userRequest.password());

        var user = User.toEntity(userRequest, encryptedPassword, this.profileService.findProfile(userRequest.idProfile()));
        this.userRepository.save(user);
    }

    @Override
    @Cacheable(value = "users", key = "#a0")
    public UserResponseDTO findByEmail(String email) {
        return Optional.ofNullable(this.userRepository.findByEmailIgnoreCase(email))
                .map(UserResponseDTO::new)
                .orElseThrow(() -> new NotFounException("Usuário não encontrado com o email: " + email));
    }

    @Override
    public User getUserByEmail(String email) {
        return this.userRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public User getUserById(Long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new NotFounException("User not found!"));
    }
}
