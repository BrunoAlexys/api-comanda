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
        if (user != null) {
            return new UserSS(
                    user.getId(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getProfiles().stream().map(p -> new SimpleGrantedAuthority(p.getName())).collect(Collectors.toSet()),
                    user.isStatus(),
                    false
            );
        }

        var employee = this.employeeRepository.findByEmailIgnoreCase(username);
        if (employee != null && employee.getActive() == StatusUser.ENABLED.getStatusValue()) {
            return new UserSS(
                    employee.getId(),
                    employee.getEmail(),
                    employee.getPassword(),
                    employee.getProfiles().stream().map(p -> new SimpleGrantedAuthority(p.getName())).collect(Collectors.toSet()),
                    employee.getActive(),
                    true
            );
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }

}
