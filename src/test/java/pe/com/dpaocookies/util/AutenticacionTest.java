package pe.com.dpaocookies.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AutenticacionTest {

    private final Autenticacion configurada = new Autenticacion(
            Map.of("DPAO_APP_USER", "demo", "DPAO_APP_PASSWORD", "clave-de-prueba")::get);

    @Test
    void aceptaSoloLaCuentaConfigurada() {
        assertTrue(configurada.valida("demo", "clave-de-prueba"));
        assertFalse(configurada.valida("demo", "otra"));
        assertFalse(configurada.valida("otro", "clave-de-prueba"));
        assertFalse(configurada.valida("demo", null));
        assertFalse(configurada.valida(null, null));
    }

    @Test
    void sinConfiguracionNadieEntra() {
        assertFalse(new Autenticacion(Map.<String, String>of()::get).valida("demo", "clave-de-prueba"));
        assertFalse(new Autenticacion(Map.of("DPAO_APP_USER", "demo", "DPAO_APP_PASSWORD", " ")::get).valida("demo", " "));
    }
}
