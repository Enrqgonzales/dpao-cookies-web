package pe.com.dpaocookies.model;

import java.math.BigDecimal;

/** Fila de {@code pack_precio}: precio de un pack de delivery por categoría y cantidad. */
public class PackPrecio {

    private final int idCategoria;
    private final String nombreCategoria;
    private final int unidades;
    private final BigDecimal precio;

    public PackPrecio(int idCategoria, String nombreCategoria, int unidades, BigDecimal precio) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.unidades = unidades;
        this.precio = precio;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public int getUnidades() {
        return unidades;
    }

    public BigDecimal getPrecio() {
        return precio;
    }
}
