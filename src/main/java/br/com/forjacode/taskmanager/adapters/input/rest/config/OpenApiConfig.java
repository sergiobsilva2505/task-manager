package br.com.forjacode.taskmanager.adapters.input.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenApiProperties.class)
public class OpenApiConfig {

    private final OpenApiProperties properties;
    private final BuildProperties buildProperties;

    public OpenApiConfig(OpenApiProperties properties, BuildProperties buildProperties) {
        this.properties = properties;
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(properties.title())
                        .description(properties.description())
                        .version(buildProperties.getVersion())
                        .contact(new Contact()
                                .name(properties.contact().name())
                                .email(properties.contact().email())))
                .servers(properties.servers().stream()
                        .map(s -> new Server().url(s.url()).description(s.description()))
                        .toList());
    }
}