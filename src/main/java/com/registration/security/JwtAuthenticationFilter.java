package com.registration.security;

import com.registration.entity.User;
import com.registration.repository.JwtTokenRepository;
import com.registration.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserRepository userRepository,
                                   JwtTokenRepository jwtTokenRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String userEmail = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                userEmail = jwtService.extractEmail(token);
            } catch (Exception e) {
            }
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);

            if (user != null && jwtService.isTokenValid(token)) {
                boolean tokenExistsInDb = jwtTokenRepository.findByToken(token).isPresent();

                if (tokenExistsInDb) {
                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(user.getEmail())
                            .password(user.getPassword())
                            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                            .build();

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    CustomAuthentication customAuth = new CustomAuthentication(authToken, user);
                    SecurityContextHolder.getContext().setAuthentication(customAuth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
