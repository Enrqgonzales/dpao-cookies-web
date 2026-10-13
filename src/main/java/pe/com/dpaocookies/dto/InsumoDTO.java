package pe.com.dpaocookies.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Datos de un insumo tal como los recibe el formulario y los muestra la vista.
 * Incluye datos derivados (categoría, alerta) que la entidad no almacena.
 */
public class InsumoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idInsumo;
    private String codigo;
    private String nombre;
    private String unidadMedida;
    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private BigDecimal costoUnitario;
    private int idCategoria;
    private String nombreCategoria;
    private boolean bajoStock;

    public int getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(int idInsumo) {
        this.idInsumo = idInsumo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public boolean isBajoStock() {
        return bajoStock;
    }

    public void setBajoStock(boolean bajoStock) {
        this.bajoStock = bajoStock;
    }
}
