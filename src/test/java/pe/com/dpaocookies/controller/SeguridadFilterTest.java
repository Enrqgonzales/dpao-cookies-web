package pe.com.dpaocookies.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SeguridadFilterTest {

    @Test
    void soloElAccesoYLaEntradaSonPublicos() {
        assertTrue(SeguridadFilter.esPublica("/acceso"));
        assertTrue(SeguridadFilter.esPublica("/app/acceso.xhtml"));
        assertTrue(SeguridadFilter.esPublica("/index.jsp"));
        assertFalse(SeguridadFilter.esPublica("/app/tablero.xhtml"));
        assertFalse(SeguridadFilter.esPublica("/app/kardex.xhtml"));
        assertFalse(SeguridadFilter.esPublica("/tablero"));
    }
}
