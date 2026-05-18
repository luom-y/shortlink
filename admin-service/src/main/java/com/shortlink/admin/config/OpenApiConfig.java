package com.shortlink.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI adminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Admin Service API")
                        .description("Admin management: user authentication, user management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Shortlink Team")
                                .email("dev@shortlink.local"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
