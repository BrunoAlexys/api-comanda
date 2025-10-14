package br.com.apicomanda.controller;

import br.com.apicomanda.dto.user.CreateUserRequest;
import br.com.apicomanda.dto.user.UserResponseDTO;
import br.com.apicomanda.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static br.com.apicomanda.helpers.ApplicationConstants.IS_ADMIN_OR_USER;
import static br.com.apicomanda.helpers.ApplicationConstants.VERSION;

@RestController
@RequestMapping(VERSION + "/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> saveUser(@RequestBody @Valid CreateUserRequest userRequest) {
        this.userService.saveUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{email}")
    @PreAuthorize(IS_ADMIN_OR_USER)
    public ResponseEntity<UserResponseDTO> findUser(@PathVariable("email") String email) {
        var userResponse = this.userService.findByEmail(email);
        return ResponseEntity.ok(userResponse);
    }
}
