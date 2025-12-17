package com.example.gestionincidents;

import javax.sql.DataSource;
import com.example.gestionincidents.security.CustomAuthenticationSuccessHandler;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/login", "/register", "/verify").permitAll()
                        // SUPER ADMIN : gestion des admins, configuration globale...
                        .requestMatchers("/superadmin/**").hasRole("SUPER_ADMIN")
                        // ADMIN : gestion des agents, incidents, etc. (SUPER_ADMIN y a accès aussi)
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        // AGENT : traitement des incidents
                        .requestMatchers("/citoyen/**").hasRole("CITOYEN")
                        // CITOYEN : espace citoyen
                        .requestMatchers("/agent/**").hasRole("AGENT")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")                // URL appelée pour se déconnecter
                        .logoutSuccessUrl("/login?logout")   // où rediriger après déconnexion
                        .invalidateHttpSession(true)         // détruit la session
                        .clearAuthentication(true)           // nettoie le contexte de sécurité
                        .deleteCookies("JSESSIONID")         // supprime le cookie de session
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource, PasswordEncoder encoder) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);

        // Créer un admin par défaut si pas encore présent
        if (!users.userExists("superadmin")) {
            UserDetails admin = User.withUsername("superadmin")
                    .password(encoder.encode("super123"))
                    .roles("SUPER_ADMIN")
                    .build();

            users.createUser(admin);
            System.out.println(">>> SUPER ADMIN JDBC créé : superadmin  / super123");
        }

        return users;
    }
}
