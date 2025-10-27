package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.exception.NotFounException;
import br.com.apicomanda.repository.ProfileRepository;
import br.com.apicomanda.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository repository;

    @Override
    public Profile findProfile(Long id) {
        return this.repository.findById(id)
                .orElseThrow(() -> new NotFounException("Perfil não encontrado"));
    }
}
