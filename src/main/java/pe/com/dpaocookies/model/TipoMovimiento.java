package pe.com.dpaocookies.model;

import java.math.BigDecimal;
import pe.com.dpaocookies.exception.NegocioException;

/**
 * Tipos admitidos por {@code movimiento_kardex.tipo_movimiento}. Las salidas se
 * guardan con signo negativo; un AJUSTE recibe el stock físico contado y la
 * variación es la diferencia con el saldo actual.
 */
public enum TipoMovimiento {
    ENTRADA(1, "Entrada"),
    SALIDA(-1, "Salida"),
    VENTA(-1, "Venta"),
    MERMA(-1, "Merma"),
    AJUSTE(0, "Ajuste por conteo");

    private final int signo;
    private final String etiqueta;

    TipoMovimiento(int signo, String etiqueta) {
        this.signo = signo;
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** {@code true} cuando el valor informado es el stock final y no una cantidad. */
    public boolean isAbsoluto() {
        return signo == 0;
    }

    public BigDecimal variacion(BigDecimal valor, BigDecimal stockAnterior) {
        if (isAbsoluto()) {
            return valor.subtract(stockAnterior);
        }
        return signo > 0 ? valor : valor.negate();
    }

    public static TipoMovimiento desde(String valor) {
        String clave = valor == null ? "" : valor.trim();
        for (TipoMovimiento tipo : values()) {
            if (tipo.name().equalsIgnoreCase(clave)) {
                return tipo;
            }
        }
        throw new NegocioException("El tipo de movimiento no es válido.");
    }
}
