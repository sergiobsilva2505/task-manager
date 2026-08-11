package br.com.forjacode.taskmanager.adapters.input.rest.config;

import br.com.forjacode.taskmanager.adapters.input.rest.exception.SecurityConfigurationException;
import br.com.forjacode.taskmanager.adapters.input.rest.security.JwtAuthenticationEntryPoint;
import br.com.forjacode.taskmanager.adapters.input.rest.security.JwtAuthenticationFilter;
import br.com.forjacode.taskmanager.application.ports.output.TokenGeneratorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        try {
            http
                    .cors(Customizer.withDefaults())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authz -> authz
                            .requestMatchers(
                                    "/swagger-ui.html",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/api/hello",
                                    "/actuator/health",
                                    "/api/users/**",
                                    "/api/auth/login",
                                    "/api/auth/google")
                            .permitAll()
                            .anyRequest().authenticated())
                    .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .csrf(csrf -> csrf.disable());

            return http.build();
        } catch (Exception e) {
            log.error("Falha ao construir a cadeia de filtros de segurança", e);
            throw new SecurityConfigurationException("Falha ao construir a cadeia de filtros de segurança");
        }
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(TokenGeneratorPort tokenGeneratorPort) {
        return new JwtAuthenticationFilter(tokenGeneratorPort);
    }
}