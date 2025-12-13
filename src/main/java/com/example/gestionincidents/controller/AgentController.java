package com.example.gestionincidents.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/agent")
public class AgentController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "agent-dashboard"; // templates/agent-dashboard.html
    }
}
