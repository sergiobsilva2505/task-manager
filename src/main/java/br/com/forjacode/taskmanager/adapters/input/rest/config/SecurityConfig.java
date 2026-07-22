package br.com.forjacode.taskmanager.adapters.input.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/hello",
                                "/api/tasks/**",
                                "/actuator/health")
                        .permitAll()
                        .anyRequest().authenticated())
                // CSRF desabilitado: API REST stateless, autenticação via Bearer/JWT (não via cookie de
                // sessão). Proteção CSRF é irrelevante nesse modelo, já que o token é enviado
                // explicitamente pelo cliente, nunca de forma automática pelo navegador.
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
