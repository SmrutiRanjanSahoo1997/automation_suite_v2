package com.automation.security;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.automation.service.AuthService;
import com.automation.service.AuthService.UserCacheDto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, AuthService authService) {
        this.jwtTokenService = jwtTokenService;
        this.authService = authService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Exempt login endpoint, static admin dashboard, admin APIs, and static assets
        if ("/api/v1/auth/login".equalsIgnoreCase(path)) {
            return true;
        }
        if (path.startsWith("/api/v1/admin/")) {
            return true;
        }
        if (path.startsWith("/static/") || path.startsWith("/assets/") || path.equals("/") || path.equals("/index.html") || path.equals("/admin/dashboard.html") || path.startsWith("/admin/")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtTokenService.extractTokenFromRequest(request);

        if (token != null && jwtTokenService.validateToken(token)) {
            String userId = jwtTokenService.extractUserId(token);
            String role = jwtTokenService.extractRole(token);

            UserCacheDto cachedUser = authService.getCachedUser(userId);
            if (cachedUser != null && cachedUser.isBlocked) {
                log.warn("Blocking request: user {} is blocked", userId);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Account is blocked. Contact administrator.");
                return;
            }

            if (userId != null && role != null) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.singletonList(authority)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } else {
            // Unauthenticated request on protected route
            String path = request.getServletPath();
            if (path.startsWith("/api/v1/")) {
                log.warn("Unauthenticated access attempt to {}", path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Valid login session or Bearer token required.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
