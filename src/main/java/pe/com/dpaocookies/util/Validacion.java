package pe.com.dpaocookies.util;

import java.math.BigDecimal;
import pe.com.dpaocookies.model.Insumo;

public final class Validacion {
    private Validacion() {}
    public static String texto(String valor, int limite, String campo) {
        if (valor == null || valor.isBlank() || valor.trim().length() > limite)
            throw new IllegalArgumentException(campo + ": obligatorio, máximo " + limite + " caracteres.");
        return valor.trim();
    }
    public static BigDecimal decimal(BigDecimal valor) {
        if (valor == null || valor.signum() < 0 || valor.scale() > 2
                || valor.compareTo(new BigDecimal("99999999.99")) > 0)
            throw new IllegalArgumentException("Valor requerido entre 0 y 99999999.99, con máximo dos decimales.");
        return valor;
    }
    public static void insumo(Insumo i) {
        if (i == null) throw new IllegalArgumentException("Insumo obligatorio.");
        i.setCodigo(texto(i.getCodigo(), 20, "Código"));
        i.setNombre(texto(i.getNombre(), 100, "Nombre"));
        i.setUnidadMedida(texto(i.getUnidadMedida(), 20, "Unidad"));
        decimal(i.getStockActual()); decimal(i.getStockMinimo()); decimal(i.getCostoUnitario());
        if (i.getIdCategoria() <= 0) throw new IllegalArgumentException("Categoría inválida.");
    }
}
