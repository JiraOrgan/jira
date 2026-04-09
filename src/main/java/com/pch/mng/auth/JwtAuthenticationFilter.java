package com.pch.mng.auth;

import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserAccountRepository userAccountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isPermitted(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        if (!jwtTokenProvider.validateAccessToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            long userId = jwtTokenProvider.getUserId(token);
            UserAccount user = userAccountRepository.findById(userId).orElse(null);
            if (user != null) {
                CustomUserDetails principal = CustomUserDetails.of(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPermitted(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if (uri.startsWith("/api/auth")) {
            return true;
        }
        if (uri.startsWith("/swagger-ui") || "/swagger-ui.html".equals(uri)) {
            return true;
        }
        if (uri.startsWith("/v3/api-docs")) {
            return true;
        }
        if ("/actuator/health".equals(uri)) {
            return true;
        }
        return "POST".equalsIgnoreCase(method) && "/api/v1/users".equals(uri);
    }
}
