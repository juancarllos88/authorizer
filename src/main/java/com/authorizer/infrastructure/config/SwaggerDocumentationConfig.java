package com.authorizer.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerDocumentationConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {

        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes("idempotencyToken", new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("idempotency-token"))
                )
                .info(apiInfo())
                ;

    }

    private Info apiInfo() {
        return new Info().title("caju-authorizer")
                .description("Transaction authorizer for benefit cards.")
                .version("v0.0.1");
    }

}
