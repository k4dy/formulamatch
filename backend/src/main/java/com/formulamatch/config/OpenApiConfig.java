package com.formulamatch.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("https://formulamatch.com"))
                .info(new Info()
                        .title("FormulaMatch API")
                        .description("Search cosmetic products and find substitutes based on ingredient similarity. " +
                                "Endpoints marked with a lock require an API key — pass it in the X-Api-Key header. " +
                                "Get your key by registering at formulamatch.com.")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("X-Api-Key", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Api-Key")));
    }
}
