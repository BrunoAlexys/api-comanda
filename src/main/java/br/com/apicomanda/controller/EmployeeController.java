package br.com.apicomanda.controller;

import br.com.apicomanda.dto.employee.CreateEmployeeDTO;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApplicationConstants.VERSION + "/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize(ApplicationConstants.IS_ADMIN)
    public ResponseEntity<Void> createEmployee(@RequestBody @Valid CreateEmployeeDTO request) {
        this.employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
