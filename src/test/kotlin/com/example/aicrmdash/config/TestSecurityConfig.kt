package com.example.aicrmdash.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Test security configuration that disables security for integration tests.
 *
 * This configuration is used in integration tests that don't need to test
 * authentication/authorization behavior.
 */
@TestConfiguration
@EnableWebSecurity
class TestSecurityConfig {

    /**
     * Creates a security filter chain that permits all requests.
     *
     * @return SecurityFilterChain that allows all requests without authentication
     */
    @Bean
    @Primary
    @Order(1)
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
        
        return http.build()
    }
}
