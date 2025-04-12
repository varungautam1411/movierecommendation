package com.movierecommender.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Movie Recommendations API",
        version = "1.0",
        description = "API for managing movie ratings and recommendations"
    )
)
public class SwaggerConfig {
}
