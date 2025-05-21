package com.example.financery.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("Configuring CORS for allowed origins: http://localhost:8080, http://localhost:63342");
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080", "http://localhost:63342")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}