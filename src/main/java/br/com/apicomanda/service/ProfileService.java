package br.com.apicomanda.service;

import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.dto.profile.CreateProfileDTO;

public interface ProfileService {
    Profile findProfile(Long id);
    void saveProfile(CreateProfileDTO profile);
}
