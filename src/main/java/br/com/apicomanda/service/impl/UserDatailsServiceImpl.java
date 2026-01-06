package br.com.apicomanda.service.impl;

import br.com.apicomanda.enums.StatusUser;
import br.com.apicomanda.repository.EmployeeRepository;
import br.com.apicomanda.repository.AdminRepository;
import br.com.apicomanda.security.UserSS;
import jakarta.transaction.Transactional;
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

    private final AdminRepository adminRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = this.adminRepository.findByEmail(username);
        if (user.isPresent()) {
            return new UserSS(
                    user.get().getId(),
                    user.get().getEmail(),
                    user.get().getPassword(),
                    user.get().getProfiles().stream().map(p -> new SimpleGrantedAuthority(p.getName())).collect(Collectors.toSet()),
                    user.get().isStatus(),
                    false
            );
        }

        var employee = this.employeeRepository.findByEmailIgnoreCase(username);
        if (employee.isPresent() && employee.get().getActive() == StatusUser.ENABLED.getStatusValue()) {
            return new UserSS(
                    employee.get().getId(),
                    employee.get().getEmail(),
                    employee.get().getPassword(),
                    employee.get().getProfiles().stream().map(p -> new SimpleGrantedAuthority(p.getName())).collect(Collectors.toSet()),
                    employee.get().getActive(),
                    true
            );
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }

}
