package br.com.apicomanda.service.impl;

import br.com.apicomanda.enums.StatusUser;
import br.com.apicomanda.repository.UserRepository;
import br.com.apicomanda.security.UserSS;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDatailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = this.userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário com o email: " + username + " não encontrado.");
        }

        var authorities = user.getProfiles().stream()
                .map(profile -> new SimpleGrantedAuthority(profile.getName()))
                .collect(Collectors.toSet());

        return new UserSS(user.getId(), user.getEmail(), user.getPassword(), authorities, user.isStatus() == StatusUser.ENABLED.getStatusValue());
    }

}
