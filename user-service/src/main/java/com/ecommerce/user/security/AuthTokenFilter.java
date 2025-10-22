package com.ecommerce.user.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Log4j2
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = resolveToken(request);
//        log.debug("JWT Filter: method={} path={}  hasToken={}", request.getMethod(), request.getServletPath(), jwt != null);

        if (jwt == null || jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtService.validateJwtToken(jwt)) {
                var claims = jwtService.extractAllClaims(jwt);
                var email = claims.getSubject();
                var role = claims.get("role", String.class);
                log.debug("JWT Filter: subject={} role={}", email, role);

                var authority = (role != null && role.startsWith("ROLE_")) ? role : "ROLE_" + role;
                var authorities = java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(authority));

                var userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(email).password("").authorities(authorities).build();

                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                auth.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("JWT Filter: context set for {} with authorities {}", email, authorities);
            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.error("Unexpected JWT filter error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts Bearer token from Authorization header.
     */
    private String resolveToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

}
