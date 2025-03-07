package com.sena._2.digitalDream.config;

import com.sena._2.digitalDream.services.Gestion_cuentas.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/login", "/registrarse", "/recuperacion/**", "/asistencia", "/procesar_login","/comprar","/perfil/**").permitAll()
                        .requestMatchers("/productos/**", "/pedidos/**", "/usuarios/**").hasRole("ADMIN")
                        .requestMatchers( "/recibos/**").hasRole("CLIENTE")
                        .requestMatchers("/img/**", "/public/img/**","/css/**","/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            authentication.getAuthorities().forEach(grantedAuthority -> {
                                try {
                                    if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
                                        response.sendRedirect("/productos/Index");
                                    } else if (grantedAuthority.getAuthority().equals("ROLE_CLIENTE")) {
                                        response.sendRedirect("/home");
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        })
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecret")
                        .tokenValiditySeconds(86400)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());  // Asegurar que usa BCryptPasswordEncoder

        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}





