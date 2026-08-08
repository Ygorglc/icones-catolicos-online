package dev.y.works.iconescatolicosonline.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT = "bearerAuth";

    @Bean
    OpenAPI apiDocumentation() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ícones Católicos Online API")
                        .version("v1")
                        .description("API para gestão de encomendas de ícones católicos artesanais."))
                .components(new Components().addSecuritySchemes(
                        JWT,
                        new SecurityScheme()
                                .name(JWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
