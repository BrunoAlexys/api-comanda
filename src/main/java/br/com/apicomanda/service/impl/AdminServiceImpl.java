package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Admin;
import br.com.apicomanda.dto.admin.CreateAdminRequest;
import br.com.apicomanda.dto.admin.AdminResponseDTO;
import br.com.apicomanda.exception.NotFoundException;
import br.com.apicomanda.exception.ObjectAlreadyRegisteredException;
import br.com.apicomanda.exception.UserNotFoundException;
import br.com.apicomanda.repository.EmployeeRepository;
import br.com.apicomanda.repository.AdminRepository;
import br.com.apicomanda.service.ProfileService;
import br.com.apicomanda.service.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public void saveUser(CreateAdminRequest userRequest) {
        if (this.adminRepository.existsByEmailIgnoreCase(userRequest.email())) {
            throw new ObjectAlreadyRegisteredException("Não foi possível concluir o cadastro. Verifique seus dados e tente novamente");
        }

        var encryptedPassword = passwordEncoder.encode(userRequest.password());

        var user = Admin.toEntity(userRequest, encryptedPassword, this.profileService.findProfile(userRequest.idProfile()));
        this.adminRepository.save(user);
    }

    @Override
    @Cacheable(value = "users", key = "#a0")
    public AdminResponseDTO findByEmail(String email) {
        var adminOptional = this.adminRepository.findByEmailIgnoreCase(email);
        if (adminOptional.isPresent()) {
            return new AdminResponseDTO(adminOptional.get());
        }

        var employeeOptional = this.employeeRepository.findByEmailIgnoreCase(email);
        if (employeeOptional.isPresent()) {
            return new AdminResponseDTO(employeeOptional.get());
        }

        throw new NotFoundException("Usuário não encontrado com o email: " + email);
    }

    @Override
    public Admin getAdminByEmail(String email) {
        return this.adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));
    }

    @Override
    public Admin getAdminById(Long id) {
        return this.adminRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado!"));
    }
}
