package cl.catastrofescl.citizen.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class UserContext {

    private UserContext() {
    }

    public static UUID obtenerUsuarioId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        String principal = auth.getPrincipal().toString();
        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException ex) {
            // El principal es el Firebase UID (no un UUID): derivamos un id determinista,
            // igual que ms-resources, para que el mismo usuario tenga el mismo id entre servicios.
            return UUID.nameUUIDFromBytes(("firebase:" + principal).getBytes(StandardCharsets.UTF_8));
        }
    }
}
