package rcm.book.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI description served at {@code /v3/api-docs} and rendered by Swagger UI at
 * {@code /swagger-ui.html}.
 *
 * <p>Declaring the bearer scheme lets the "Authorize" button in Swagger UI attach a JWT, so the
 * secured {@code /me} endpoints can be exercised straight from the browser.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI bookTrackerOpenApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("Book Tracker API")
                        .version("v1")
                        .description("""
                                Track a personal reading list backed by Open Library.

                                Obtain a token from POST /api/v1/auth/login and send it as
                                `Authorization: Bearer <token>`. Every /me endpoint resolves the
                                user from that token, so no user id is ever passed by the client."""))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .name(BEARER_SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
