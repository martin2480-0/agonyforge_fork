package com.agonyforge.mud.demo.service;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class UserPrincipalService {

    @GetMapping("/principal")
    public String getPrincipal(@AuthenticationPrincipal Principal principal) {
        return principal.getName();
    }
}
