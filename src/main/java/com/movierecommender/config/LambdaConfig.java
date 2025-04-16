package com.movierecommender.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;

@Configuration
@ComponentScan(
    basePackages = "com.movierecommender",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {WebMvcAutoConfiguration.class, WebMvcConfigurationSupport.class}
    )
)
public class LambdaConfig {
    // Add any additional beans if needed
}
