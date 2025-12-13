package com.example.gestionincidents.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CitizenController {

    @GetMapping("/citoyen/dashboard")
    public String citizenDashboard() {

        return "citizen-dashboard";
    }
}
