package com.example.gestionincidents.controller;

import com.example.gestionincidents.service.ConnectedUserInfoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/citoyen")
public class CitizenController {

    private final ConnectedUserInfoService connectedUserInfoService;

    public CitizenController(ConnectedUserInfoService connectedUserInfoService) {
        this.connectedUserInfoService = connectedUserInfoService;
    }

    @GetMapping("/dashboard")
    public String citizenDashboard(Model model, Authentication authentication) {

        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        return "citizen-dashboard";
    }
}


