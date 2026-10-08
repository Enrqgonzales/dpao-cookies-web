package pe.com.dpaocookies.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Fila de {@code movimiento_kardex}. Código y nombre del ítem no son columnas de
 * la tabla: se completan con el JOIN de la consulta o con la fila bloqueada.
 */
public class MovimientoKardex {

    private long idMovimiento;
    private ClaseItem claseItem;
    private int idItem;
    private String codigoItem;
    private String nombreItem;
    private TipoMovimiento tipoMovimiento;
    private BigDecimal cantidad;
    private BigDecimal stockAnterior;
    private BigDecimal stockPosterior;
    private String observacion;
    private Timestamp fechaRegistro;

    public long getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(long idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public ClaseItem getClaseItem() {
        return claseItem;
    }

    public void setClaseItem(ClaseItem claseItem) {
        this.claseItem = claseItem;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public String getCodigoItem() {
        return codigoItem;
    }

    public void setCodigoItem(String codigoItem) {
        this.codigoItem = codigoItem;
    }

    public String getNombreItem() {
        return nombreItem;
    }

    public void setNombreItem(String nombreItem) {
        this.nombreItem = nombreItem;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(BigDecimal stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public BigDecimal getStockPosterior() {
        return stockPosterior;
    }

    public void setStockPosterior(BigDecimal stockPosterior) {
        this.stockPosterior = stockPosterior;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
