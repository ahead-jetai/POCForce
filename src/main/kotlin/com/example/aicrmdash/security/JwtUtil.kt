package com.example.aicrmdash.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

/**
 * Utility service for JWT token generation and validation.
 *
 * Handles creation of JWT tokens for authenticated users and validation
 * of incoming tokens for secured endpoints.
 */
@Component
class JwtUtil(
    @Value("\${jwt.secret:defaultSecretKeyForDevelopmentOnlyPleaseChangeInProduction}")
    private val secret: String,
    
    @Value("\${jwt.expiration:86400000}") // 24 hours in milliseconds
    private val expirationMs: Long
) {
    
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    /**
     * Generates a JWT token for the given username.
     *
     * @param username The username to include in the token
     * @return JWT token string
     */
    fun generateToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Extracts the username from a JWT token.
     *
     * @param token JWT token string
     * @return Username from token
     */
    fun getUsernameFromToken(token: String): String {
        return getClaims(token).subject
    }

    /**
     * Validates a JWT token.
     *
     * @param token JWT token string
     * @return true if token is valid, false otherwise
     */
    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extracts claims from a JWT token.
     *
     * @param token JWT token string
     * @return Claims object
     */
    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
