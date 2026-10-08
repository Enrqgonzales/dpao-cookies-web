package pe.com.dpaocookies.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/** Línea del Kardex lista para mostrarse: identifica el ítem y los saldos. */
public class MovimientoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long idMovimiento;
    private String claseItem;
    private int idItem;
    private String codigoItem;
    private String nombreItem;
    private String tipoMovimiento;
    private BigDecimal cantidad;
    private BigDecimal stockAnterior;
    private BigDecimal stockPosterior;
    private String observacion;
    private Date fechaRegistro;

    /** Entrada cuando la variación es positiva; salida cuando es negativa. */
    public boolean isEntrada() {
        return cantidad != null && cantidad.signum() > 0;
    }

    public long getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(long idMovimiento) {
        this.idMovimiento = idMovimiento;
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

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
