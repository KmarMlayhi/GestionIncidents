package com.example.gestionincidents.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // Page de login
    @GetMapping("/login")
    public String login() {
        return "login"; // templates/login.html
    }
}
