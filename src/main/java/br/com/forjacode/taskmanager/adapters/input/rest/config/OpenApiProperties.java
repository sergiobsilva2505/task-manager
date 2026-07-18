package br.com.forjacode.taskmanager.adapters.input.rest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.openapi")
public record OpenApiProperties(
        String title,
        String description,
        Contact contact,
        List<ServerInfo> servers
) {
    public record Contact(String name, String email) {}
    public record ServerInfo(String url, String description) {}
}