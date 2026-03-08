package br.com.apicomanda.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserSS implements UserDetails {
    @Getter
    private Long id;
    private String email;
    private String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    @Getter
    private final boolean isEmployee;

    public UserSS(Long id, String email, String password, Collection<? extends GrantedAuthority> authorities, boolean enabled, boolean isEmployee) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.isEmployee = isEmployee;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
