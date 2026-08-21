package com.rahul.realtime.notification.config.dev;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class DevJwtConfig {

    @Bean
    public JwtEncoder jwtEncoder() throws Exception {

        PublicKey publicKey = loadPublicKey();

        PrivateKey privateKey = loadPrivateKey();

        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        return NimbusJwtEncoder.withKeyPair((java.security.interfaces.RSAPublicKey) publicKey, (java.security.interfaces.RSAPrivateKey) privateKey).build();
    }

    private PublicKey loadPublicKey() throws Exception {

        ClassPathResource resource = new ClassPathResource("security/public_key.pem");

        String key = new String(resource.getInputStream().readAllBytes());

        key = key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private PrivateKey loadPrivateKey() throws Exception {

        ClassPathResource resource = new ClassPathResource("security/private_key.pem");

        String key = new String(resource.getInputStream().readAllBytes());

        key = key.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}