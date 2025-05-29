package com.alberto.mpesa.api.store.infra.Security;

import com.alberto.mpesa.api.store.Repository.AdminRepository;
import com.alberto.mpesa.api.store.domain.model.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = this.adminRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + username));

        return new User(
                admin.getEmail(),
                admin.getPassword(),
                admin.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList()
        );

    }
}
