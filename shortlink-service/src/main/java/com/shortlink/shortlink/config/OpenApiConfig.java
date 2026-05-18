package com.shortlink.shortlink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shortlinkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shortlink Service API")
                        .description("Core short link service: generate, redirect, cache, statistics")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Shortlink Team")
                                .email("dev@shortlink.local"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
