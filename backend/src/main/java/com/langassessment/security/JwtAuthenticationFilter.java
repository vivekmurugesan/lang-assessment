package com.langassessment.security;

import com.langassessment.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (!jwtTokenProvider.validateToken(jwt)) {
                    log.warn("JWT token validation failed for request path: {}", request.getRequestURI());
                } else {
                    String email = jwtTokenProvider.getEmailFromToken(jwt);
                    String role = jwtTokenProvider.getRoleFromToken(jwt);

                    log.debug("JWT Token validated. Email: {}, Role: {}, Path: {}", email, role, request.getRequestURI());

                    var user = userService.findByEmail(email);
                    if (user.isPresent()) {
                        var authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + role)
                        );
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user.get(), null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("Authentication set for user: {} with role: ROLE_{}", email, role);
                    } else {
                        log.warn("User not found in database: {}", email);
                    }
                }
            } else {
                log.debug("No JWT token found in request for path: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // Try to get token from Authorization header first
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Fallback to query parameter for audio/resource requests
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            log.debug("JWT token found in query parameter for path: {}", request.getRequestURI());
            return tokenParam;
        }

        return null;
    }
}
