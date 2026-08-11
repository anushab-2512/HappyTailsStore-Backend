package com.registration.security;

import com.registration.entity.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Custom Authentication that wraps the standard UsernamePasswordAuthenticationToken
 * and also holds the full User entity, so controllers can retrieve it via
 * SecurityContextHolder.getContext().getAuthentication()
 */
public class CustomAuthentication extends AbstractAuthenticationToken {

    private final Object credentials;
    private final Object principal;
    private final User user;

    public CustomAuthentication(Object principal, User user) {
        super(extractAuthorities(principal));
        this.principal = principal;
        this.credentials = null;
        this.user = user;
        setAuthenticated(true);
    }

    private static java.util.Collection<? extends GrantedAuthority> extractAuthorities(Object principal) {
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return userDetails.getAuthorities();
        }
        if (principal instanceof org.springframework.security.core.Authentication auth) {
            return auth.getAuthorities();
        }
        return new java.util.ArrayList<>();
    }

    public User getUser() {
        return user;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
