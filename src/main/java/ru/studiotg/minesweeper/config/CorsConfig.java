package ru.studiotg.minesweeper.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/*") // запросы начинающиеся с /api/
                .allowedOrigins("http://localhost:8080", "https://minesweeper-test.studiotg.ru/") // разрешенные источники
                .allowedMethods("POST", "OPTIONS")
                .maxAge(3600); // время кеширования preflight запросов
    }
}
