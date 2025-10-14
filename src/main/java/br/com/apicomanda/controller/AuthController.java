package br.com.apicomanda.controller;

import br.com.apicomanda.dto.auth.RefreshTokenDTO;
import br.com.apicomanda.dto.auth.TokenResponse;
import br.com.apicomanda.dto.auth.CredentialRequestDTO;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApplicationConstants.VERSION + "/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid CredentialRequestDTO data) {
        var tokenResponse = this.authServiceImpl.login(data);
        if (tokenResponse.accessToken().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenResponse);
        }

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody @Valid RefreshTokenDTO refreshToken) {
        var tokenResponse = this.authServiceImpl.refreshToken(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }
}
