package br.com.apicomanda.controller;

import br.com.apicomanda.dto.profile.CreateProfileDTO;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApplicationConstants.VERSION + "/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<Void> saveProfile(@RequestBody @Valid CreateProfileDTO request) {
        this.profileService.saveProfile(request);
        return ResponseEntity.status(201).build();
    }
}
