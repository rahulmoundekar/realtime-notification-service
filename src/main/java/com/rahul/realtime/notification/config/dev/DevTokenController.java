package com.rahul.realtime.notification.config.dev;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class DevTokenController {

    private final JwtEncoder jwtEncoder;

    @GetMapping("/dev/token")
    public String token(@RequestParam(defaultValue = "user-101") String userId) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder().subject(userId).issuedAt(now).expiresAt(now.plusSeconds(3600)).claim("roles", "USER").build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}