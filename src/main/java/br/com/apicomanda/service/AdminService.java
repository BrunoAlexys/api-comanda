package br.com.apicomanda.service;

import br.com.apicomanda.domain.Admin;
import br.com.apicomanda.dto.admin.CreateAdminRequest;
import br.com.apicomanda.dto.admin.AdminResponseDTO;

public interface AdminService {
    void saveUser(CreateAdminRequest userRequest);
    AdminResponseDTO findByEmail(String email);
    Admin getAdminByEmail(String email);
    Admin getAdminById(Long id);
}
