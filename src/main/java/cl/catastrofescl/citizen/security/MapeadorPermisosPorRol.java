package cl.catastrofescl.citizen.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Deriva los permisos de ciudadania a partir de los roles del usuario.
 *
 * <p>ms-identity solo escribe el claim {@code roles} en el token Firebase (no escribe
 * {@code permissions}), por lo que cada microservicio debe resolver sus propios permisos
 * a partir del rol. Este mapa es el espejo del seed V5 de ms-identity para el modulo CIUDADANIA.
 */
public final class MapeadorPermisosPorRol {

    public static final String DONACION_REALIZAR = "DONACION_REALIZAR";
    public static final String DONACION_CONFIRMAR = "DONACION_CONFIRMAR";
    public static final String NECESIDAD_GESTIONAR = "NECESIDAD_GESTIONAR";

    /** Alias de roles emitidos por Firebase (ingles) hacia el codigo interno (espanol). */
    private static final Map<String, String> ALIAS_ROL = Map.of(
            "ADMIN", "ADMINISTRADOR",
            "AUTHORITY", "AUTORIDAD",
            "OPERATOR", "OPERADOR",
            "CITIZEN", "PARTICULAR",
            "VOLUNTEER", "VOLUNTARIO"
    );

    private static final Map<String, Set<String>> PERMISOS_POR_ROL = Map.of(
            "ADMINISTRADOR", Set.of(DONACION_REALIZAR, DONACION_CONFIRMAR, NECESIDAD_GESTIONAR),
            "AUTORIDAD", Set.of(DONACION_CONFIRMAR, NECESIDAD_GESTIONAR),
            "OPERADOR", Set.of(DONACION_CONFIRMAR, NECESIDAD_GESTIONAR),
            "PARTICULAR", Set.of(DONACION_REALIZAR),
            "VOLUNTARIO", Set.of(DONACION_REALIZAR)
    );

    private MapeadorPermisosPorRol() {
    }

    /** Normaliza un rol a su codigo interno en mayuscula (resuelve alias en ingles). */
    public static String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return null;
        }
        String normalizado = rol.trim().toUpperCase(Locale.ROOT);
        return ALIAS_ROL.getOrDefault(normalizado, normalizado);
    }

    /** Permisos del modulo ciudadania concedidos por la coleccion de roles indicada. */
    public static Set<String> permisosPara(Collection<String> roles) {
        Set<String> permisos = new HashSet<>();
        if (roles == null) {
            return permisos;
        }
        for (String rol : roles) {
            Set<String> delRol = PERMISOS_POR_ROL.get(normalizarRol(rol));
            if (delRol != null) {
                permisos.addAll(delRol);
            }
        }
        return permisos;
    }
}
