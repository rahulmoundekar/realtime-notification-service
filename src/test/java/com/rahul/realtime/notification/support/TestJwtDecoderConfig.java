package com.rahul.realtime.notification.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration(proxyBeanMethods = false)
public class TestJwtDecoderConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {

        return token -> Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .subject("user-101")
                .claim("roles", "USER")
                .issuedAt(java.time.Instant.now())
                .expiresAt(
                        java.time.Instant.now()
                                .plusSeconds(3600)
                )
                .build();
    }
}