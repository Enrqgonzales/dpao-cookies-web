package pe.com.dpaocookies.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Pack de delivery de una categoría: sus sabores y el precio por cantidad de galletas. */
public class PackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int idCategoria;
    private final String categoria;
    private final List<String> sabores;
    private final List<Precio> precios;

    public PackDTO(int idCategoria, String categoria, List<String> sabores, List<Precio> precios) {
        this.idCategoria = idCategoria;
        this.categoria = categoria;
        this.sabores = List.copyOf(sabores);
        this.precios = List.copyOf(precios);
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public List<String> getSabores() {
        return sabores;
    }

    public List<Precio> getPrecios() {
        return precios;
    }

    /** Precio de un pack según la cantidad de galletas. */
    public static class Precio implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int unidades;
        private final BigDecimal precio;

        public Precio(int unidades, BigDecimal precio) {
            this.unidades = unidades;
            this.precio = precio;
        }

        public int getUnidades() {
            return unidades;
        }

        public BigDecimal getPrecio() {
            return precio;
        }
    }
}
