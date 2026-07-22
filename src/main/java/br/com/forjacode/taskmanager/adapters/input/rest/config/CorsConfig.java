package br.com.forjacode.taskmanager.adapters.input.rest.config;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    private final String corsAddress;
    private final String[] allowedOrigins;
    private final String[] allowedMethods;
    private final String[] allowedHeaders;

    public CorsConfig(
            @Value("${app.cors.address}") String corsAddress,
            @Value("${app.cors.allowed-origins}") String[] allowedOrigins,
            @Value("${app.cors.allowed-methods}") String[] allowedMethods,
            @Value("${app.cors.allowed-headers}") String[] allowedHeaders) {
        this.corsAddress = corsAddress;
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping(corsAddress)
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods(allowedMethods)
                        .allowedHeaders(allowedHeaders);
            }
        };
    }
}
