package com.rahul.realtime.notification.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    public String getCurrentUserId(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authenticated user not found"
            );
        }

        return authentication.getName();
    }
}