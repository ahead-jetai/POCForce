package com.example.aicrmdash.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * CORS configuration for frontend integration.
 * Allows configurable origins, methods, and headers for cross-origin requests.
 */
@Configuration
class CorsConfig(
    @Value("\${cors.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private val allowedOrigins: String
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(*allowedOrigins.split(",").toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("Content-Type", "Authorization", "Accept", "X-Requested-With")
            .allowCredentials(true)
            .maxAge(3600)
    }
}
