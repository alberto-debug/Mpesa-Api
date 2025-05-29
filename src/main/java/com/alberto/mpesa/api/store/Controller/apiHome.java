package com.alberto.mpesa.api.store.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class apiHome {

    @GetMapping("/")

    public String home(){
        return "Application currently running at port: 8080";
    }
}
