package com.example.gestionincidents.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isAgent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));

        boolean isCitizen = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CITOYEN"));

        if (isAdmin) {
            response.sendRedirect("/admin");
        } else if (isAgent) {
            response.sendRedirect("/agent/dashboard");
        } else if (isCitizen) {
            response.sendRedirect("/citoyen/dashboard");
        } else {
            // au cas où, fallback
            response.sendRedirect("/");
        }
    }
}
