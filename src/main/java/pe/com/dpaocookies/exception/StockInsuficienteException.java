package pe.com.dpaocookies.exception;

import java.math.BigDecimal;

/** El movimiento dejaría las existencias por debajo de cero. */
public class StockInsuficienteException extends NegocioException {

    private final BigDecimal disponible;
    private final BigDecimal solicitado;

    public StockInsuficienteException(BigDecimal disponible, BigDecimal solicitado) {
        super("Stock insuficiente: disponible " + disponible.toPlainString()
                + ", solicitado " + solicitado.toPlainString() + ".");
        this.disponible = disponible;
        this.solicitado = solicitado;
    }

    public BigDecimal getDisponible() {
        return disponible;
    }

    public BigDecimal getSolicitado() {
        return solicitado;
    }
}
