package com.github.arsenmonets.newshub.security;

import com.github.arsenmonets.newshub.dto.AuthenticatedUserDTO;
import com.github.arsenmonets.newshub.models.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver resolver;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    public JwtAuthenticationFilter(JwtUtil jwtUtil, HandlerExceptionResolver resolver) {
        this.jwtUtil = jwtUtil;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());

                if (!jwtUtil.validateToken(token)) {
                    throw new JwtException("");
                }

                String username = jwtUtil.extractUsername(token);
                String roleString = jwtUtil.extractRole(token);
                Long userId = jwtUtil.extractUserId(token);
                UserRole role = UserRole.valueOf(roleString);

                AuthenticatedUserDTO authenticatedUser = new AuthenticatedUserDTO(userId, username, role);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        authenticatedUser, null, authenticatedUser.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            resolver.resolveException(request, response, null, new JwtException("Токен вичерпано"));
        } catch (JwtException e) {
            resolver.resolveException(request, response, null, new JwtException("Невалідний токен"));
        } catch (Exception e) {
            resolver.resolveException(request, response, null, new JwtException("Помилка аутентифікації"));
        }
    }
}
