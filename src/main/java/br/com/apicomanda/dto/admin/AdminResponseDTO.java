package br.com.apicomanda.dto.admin;

import br.com.apicomanda.domain.Employee;
import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.domain.Admin;

import java.util.List;

public record AdminResponseDTO(
        Long id,
        String name,
        String email,
        String telephone,
        List<String> profiles,
        Long adminId
) {
    public AdminResponseDTO(Admin admin) {
        this(admin.getId(), admin.getName(), admin.getEmail(), admin.getTelephone(),
                admin.getProfiles().stream()
                        .map(Profile::getName)
                        .toList(), null);
    }

    public AdminResponseDTO(Employee employee) {
        this(employee.getId(), employee.getName(), employee.getEmail(), employee.getTelephone(),
                employee.getProfiles().stream()
                        .map(Profile::getName)
                        .toList(), employee.getAdmin().getId());
    }
}