package com.rahul.realtime.notification.support;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;

import java.time.Instant;

public final class TestJwtFactory {

    private TestJwtFactory() {
    }

    public static JwtClaimsSet user(String userId) {

        Instant now = Instant.now();

        return JwtClaimsSet.builder().subject(userId).issuedAt(now).expiresAt(now.plusSeconds(3600)).claim("roles", "USER").build();
    }
}