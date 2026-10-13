package pe.com.dpaocookies.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Datos de entrada de un movimiento de Kardex. En un AJUSTE, {@code cantidad}
 * es el stock físico contado; en los demás tipos, la cantidad movida (positiva).
 */
public class SolicitudMovimientoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String claseItem;
    private int idItem;
    private String tipoMovimiento;
    private BigDecimal cantidad;
    private String motivo;

    public SolicitudMovimientoDTO() {
    }

    public SolicitudMovimientoDTO(String claseItem, int idItem, String tipoMovimiento,
            BigDecimal cantidad, String motivo) {
        this.claseItem = claseItem;
        this.idItem = idItem;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
    }

    public String getClaseItem() {
        return claseItem;
    }

    public void setClaseItem(String claseItem) {
        this.claseItem = claseItem;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
