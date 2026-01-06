package br.com.apicomanda.controller;

import br.com.apicomanda.dto.admin.CreateAdminRequest;
import br.com.apicomanda.dto.admin.AdminResponseDTO;
import br.com.apicomanda.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static br.com.apicomanda.helpers.ApplicationConstants.IS_ADMIN_OR_USER;
import static br.com.apicomanda.helpers.ApplicationConstants.VERSION;

@RestController
@RequestMapping(VERSION + "/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<Void> saveAdmin(@RequestBody @Valid CreateAdminRequest adminRequest) {
        this.adminService.saveUser(adminRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{email}")
    @PreAuthorize(IS_ADMIN_OR_USER)
    public ResponseEntity<AdminResponseDTO> findAdmin(@PathVariable("email") String email) {
        var userResponse = this.adminService.findByEmail(email);
        return ResponseEntity.ok(userResponse);
    }
}
