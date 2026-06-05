package cl.catastrofescl.citizen.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DevAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-Dev-User-Id";
    private static final String HEADER_ROLES = "X-Dev-Roles";
    private static final String HEADER_PERMISSIONS = "X-Dev-Permissions";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = request.getHeader(HEADER_USER_ID);
            if (userId != null && !userId.isBlank()) {
                List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();

                String roles = request.getHeader(HEADER_ROLES);
                if (roles != null && !roles.isBlank()) {
                    Arrays.stream(roles.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .forEach(authorities::add);
                }

                String permissions = request.getHeader(HEADER_PERMISSIONS);
                if (permissions != null && !permissions.isBlank()) {
                    Arrays.stream(permissions.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(SimpleGrantedAuthority::new)
                            .forEach(authorities::add);
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
