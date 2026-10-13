package pe.com.dpaocookies.model;

import pe.com.dpaocookies.exception.NegocioException;

/**
 * Ítems inventariables del Kardex. La tabla y la columna de cada ítem salen de
 * esta lista fija y nunca de un parámetro del usuario.
 */
public enum ClaseItem {
    INSUMO("insumo", "id_insumo", "Insumo"),
    PRODUCTO("producto", "id_producto", "Producto");

    private final String tabla;
    private final String columnaId;
    private final String etiqueta;

    ClaseItem(String tabla, String columnaId, String etiqueta) {
        this.tabla = tabla;
        this.columnaId = columnaId;
        this.etiqueta = etiqueta;
    }

    public String getTabla() {
        return tabla;
    }

    public String getColumnaId() {
        return columnaId;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static ClaseItem desde(String valor) {
        String clave = valor == null ? "" : valor.trim();
        for (ClaseItem clase : values()) {
            if (clase.name().equalsIgnoreCase(clave)) {
                return clase;
            }
        }
        throw new NegocioException("La clase de ítem no es válida.");
    }
}
