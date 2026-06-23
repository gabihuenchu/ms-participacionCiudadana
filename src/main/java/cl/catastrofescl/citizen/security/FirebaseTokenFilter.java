package cl.catastrofescl.citizen.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HEADER_AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            try {
                FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
                List<SimpleGrantedAuthority> authorities = extraerAuthorities(decoded);
                String principal = decoded.getClaims().get("usuarioId") != null
                        ? decoded.getClaims().get("usuarioId").toString()
                        : decoded.getUid();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (FirebaseAuthException ex) {
                log.warn("Token Firebase inválido: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> extraerAuthorities(FirebaseToken token) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Map<String, Object> claims = token.getClaims();

        List<String> roles = new ArrayList<>();
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?> rolesList) {
            for (Object rol : rolesList) {
                String interno = MapeadorPermisosPorRol.normalizarRol(rol.toString());
                if (interno == null) {
                    continue;
                }
                roles.add(interno);
                authorities.add(new SimpleGrantedAuthority("ROLE_" + interno));
            }
        }

        // ms-identity solo escribe el claim roles; derivamos los permisos de ciudadania del rol.
        MapeadorPermisosPorRol.permisosPara(roles)
                .forEach(permiso -> authorities.add(new SimpleGrantedAuthority(permiso)));

        // Compatibilidad: si el token llegara con permisos explicitos, tambien se respetan.
        Object permissionsClaim = claims.get("permissions");
        if (permissionsClaim instanceof List<?> permissions) {
            permissions.stream()
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        return authorities;
    }
}
