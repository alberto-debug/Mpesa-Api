package com.alberto.mpesa.api.store.domain.Role;

import com.alberto.mpesa.api.store.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class RoleSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;


    @Override
    public void run(String... args) throws Exception {

        if (roleRepository.findByName("ROLE_GUEST").isEmpty()){
            roleRepository.save(new Role(null, "ROLE_GUEST", new HashSet<>()));

            if (roleRepository.findByName("ROLE_ADMIN").isEmpty()){
                roleRepository.save(new Role(null,"ROLE_ADMIN", new HashSet<>()));
            }
        }
    }
}
