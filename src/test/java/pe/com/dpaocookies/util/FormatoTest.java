package pe.com.dpaocookies.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormatoTest {

    @Test
    void montosConPuntoDecimalYComaDeMiles() {
        assertEquals("373.50", Formato.monto(new BigDecimal("373.5")));
        assertEquals("1,541.50", Formato.monto(new BigDecimal("1541.499")));
        assertEquals("", Formato.monto(null));
    }

    @Test
    void enteroYDecimalesPorSeparadoParaElDinero() {
        assertEquals("1,541", Formato.entero(new BigDecimal("1541.50")));
        assertEquals(".50", Formato.decimales(new BigDecimal("1541.50")));
        assertEquals("−17", Formato.entero(new BigDecimal("-17")));
    }

    @Test
    void cantidadesDelKardexLlevanSigno() {
        assertEquals("+6.00", Formato.conSigno(new BigDecimal("6")));
        assertEquals("−3.00", Formato.conSigno(new BigDecimal("-3")));
        assertEquals("0.00", Formato.conSigno(BigDecimal.ZERO));
    }

    @Test
    void unidadesSinDecimalesCuandoSonEnteras() {
        assertEquals("43", Formato.unidades(new BigDecimal("43.00")));
        assertEquals("2.50", Formato.unidades(new BigDecimal("2.5")));
        assertEquals("0", Formato.unidades(new BigDecimal("0.00")));
    }

    @Test
    void enumeraComoSeLee() {
        assertEquals("Nutella, Snicker y Pistacho", Formato.enumerar(List.of("Nutella", "Snicker", "Pistacho")));
        assertEquals("Chispas", Formato.enumerar(List.of("Chispas")));
        assertEquals("", Formato.enumerar(List.of()));
    }
}
