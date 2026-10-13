package pe.com.dpaocookies.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** Lo vendido de un sabor en el día: unidades, monto y su proporción frente al más vendido. */
public class VentaSaborDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final String nombre;
    private final BigDecimal unidades;
    private final BigDecimal monto;
    private final int porcentaje;

    public VentaSaborDTO(String codigo, String nombre, BigDecimal unidades, BigDecimal monto, int porcentaje) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.unidades = unidades;
        this.monto = monto;
        this.porcentaje = porcentaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getUnidades() {
        return unidades;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    /** 0 a 100, relativo al sabor que más vendió; sirve para el ancho de la barra. */
    public int getPorcentaje() {
        return porcentaje;
    }
}
