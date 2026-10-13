package pe.com.dpaocookies.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.function.Function;

/**
 * Valida la cuenta de acceso configurada fuera del código (DPAO_APP_USER y DPAO_APP_PASSWORD).
 * La usan el servlet de acceso (JSP) y el bean de acceso (JSF): la regla vive en un solo lugar.
 */
public class Autenticacion {

    private final Function<String, String> configuracion;

    public Autenticacion() {
        this(System::getenv);
    }

    public Autenticacion(Function<String, String> configuracion) {
        this.configuracion = configuracion;
    }

    /** {@code true} si la cuenta está configurada y coincide; compara la clave en tiempo constante. */
    public boolean valida(String usuario, String clave) {
        String usuarioEsperado = configuracion.apply("DPAO_APP_USER");
        String claveEsperada = configuracion.apply("DPAO_APP_PASSWORD");
        return usuarioEsperado != null && claveEsperada != null && !claveEsperada.isBlank()
                && usuarioEsperado.equals(usuario) && clave != null
                && MessageDigest.isEqual(claveEsperada.getBytes(StandardCharsets.UTF_8),
                        clave.getBytes(StandardCharsets.UTF_8));
    }
}
