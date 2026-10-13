package pe.com.dpaocookies.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

/**
 * Formato de montos y cantidades de la interfaz: punto decimal y coma de miles (es-PE),
 * siempre con dos decimales. Los símbolos se fijan aquí para no depender del JDK.
 */
public final class Formato {

    private static final DecimalFormatSymbols SIMBOLOS = new DecimalFormatSymbols();

    static {
        SIMBOLOS.setDecimalSeparator('.');
        SIMBOLOS.setGroupingSeparator(',');
        SIMBOLOS.setMinusSign('−');
    }

    private Formato() {
    }

    /** {@code 1541.5} → {@code "1,541.50"}; {@code null} → cadena vacía. */
    public static String monto(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        DecimalFormat formato = new DecimalFormat("#,##0.00", SIMBOLOS);
        return formato.format(valor.setScale(2, RoundingMode.HALF_UP));
    }

    /** Parte entera con miles: {@code 1541.50} → {@code "1,541"} (incluye el signo). */
    public static String entero(BigDecimal valor) {
        String texto = monto(valor);
        int punto = texto.lastIndexOf('.');
        return punto < 0 ? texto : texto.substring(0, punto);
    }

    /** Decimales con su punto: {@code 1541.50} → {@code ".50"}. */
    public static String decimales(BigDecimal valor) {
        String texto = monto(valor);
        int punto = texto.lastIndexOf('.');
        return punto < 0 ? "" : texto.substring(punto);
    }

    /** Cantidad con signo explícito para el Kardex: {@code +6.00} / {@code −3.00}. */
    public static String conSigno(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return valor.signum() > 0 ? "+" + monto(valor) : monto(valor);
    }

    /** Lista para leer: {@code [a, b, c]} → {@code "a, b y c"}. */
    public static String enumerar(List<String> elementos) {
        if (elementos == null || elementos.isEmpty()) {
            return "";
        }
        int ultimo = elementos.size() - 1;
        return ultimo == 0 ? elementos.get(0)
                : String.join(", ", elementos.subList(0, ultimo)) + " y " + elementos.get(ultimo);
    }

    /** Unidades enteras sin decimales cuando no los tienen: {@code 43.00} → {@code "43"}. */
    public static String unidades(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        BigDecimal limpio = valor.stripTrailingZeros();
        return limpio.scale() <= 0 ? limpio.toBigInteger().toString() : monto(valor);
    }
}
