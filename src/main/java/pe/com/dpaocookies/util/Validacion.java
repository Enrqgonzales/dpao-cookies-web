package pe.com.dpaocookies.util;

import java.math.BigDecimal;
import pe.com.dpaocookies.exception.NegocioException;

/** Reglas de formato compartidas; coinciden con las columnas DECIMAL(10,2) y VARCHAR. */
public final class Validacion {

    public static final BigDecimal MAXIMO = new BigDecimal("99999999.99");

    private Validacion() {
    }

    public static String texto(String valor, int limite, String campo) {
        if (valor == null || valor.isBlank() || valor.trim().length() > limite) {
            throw new NegocioException(campo + ": obligatorio, máximo " + limite + " caracteres.");
        }
        return valor.trim();
    }

    public static BigDecimal decimal(BigDecimal valor) {
        if (valor == null || valor.signum() < 0 || valor.scale() > 2 || valor.compareTo(MAXIMO) > 0) {
            throw new NegocioException("Valor requerido entre 0 y 99999999.99, con máximo dos decimales.");
        }
        return valor;
    }

    public static int identificador(int valor, String mensaje) {
        if (valor <= 0) {
            throw new NegocioException(mensaje);
        }
        return valor;
    }
}
