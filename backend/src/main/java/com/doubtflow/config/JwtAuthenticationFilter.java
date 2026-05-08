package com.doubtflow.config;

import com.doubtflow.model.UserPrincipal;
import com.doubtflow.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/health",
            "/api/auth/login",
            "/api/auth/student/register",
            "/api/auth/mentor/register",
            "/api/students/register",
            "/api/students/login"
    );

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) || PUBLIC_PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserPrincipal user = jwtService.parseUser(token);
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, List.of(authority));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException ignored) {
                // Invalid tokens stay unauthenticated and are rejected by protected endpoints.
            }
        }

        filterChain.doFilter(request, response);
    }
}
