package br.com.forjacode.taskmanager.adapters.output.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google")
public record GoogleOAuthProperties(String clientId) {
}
