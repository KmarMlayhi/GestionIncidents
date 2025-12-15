package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import com.example.gestionincidents.service.ConnectedUserInfoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/agent")
public class AgentController {

    private final ConnectedUserInfoService connectedUserInfoService;


    public AgentController(ConnectedUserInfoService connectedUserInfoService) {

        this.connectedUserInfoService = connectedUserInfoService;
    }

    @GetMapping("/dashboard")
    public String agentDashboard(Model model, Authentication authentication) {

        connectedUserInfoService.addConnectedUserInfo(model, authentication);
        return "agent-dashboard";
    }
}
