package pe.com.dpaocookies.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Indicadores del tablero reunidos por la fachada en una sola llamada. */
public class ResumenTableroDTO implements Serializable {

    private static final long serialVersionUID = 2L;

    private final int totalInsumos;
    private final int totalProductos;
    private final List<InsumoDTO> alertas;
    private final List<MovimientoDTO> ultimosMovimientos;
    private final BigDecimal unidadesVendidasHoy;
    private final BigDecimal montoVendidoHoy;
    private final List<VentaSaborDTO> masVendidos;
    private final List<ProductoDTO> agotados;

    public ResumenTableroDTO(int totalInsumos, int totalProductos, List<InsumoDTO> alertas,
            List<MovimientoDTO> ultimosMovimientos) {
        this(totalInsumos, totalProductos, alertas, ultimosMovimientos, BigDecimal.ZERO, BigDecimal.ZERO,
                List.of(), List.of());
    }

    public ResumenTableroDTO(int totalInsumos, int totalProductos, List<InsumoDTO> alertas,
            List<MovimientoDTO> ultimosMovimientos, BigDecimal unidadesVendidasHoy, BigDecimal montoVendidoHoy,
            List<VentaSaborDTO> masVendidos, List<ProductoDTO> agotados) {
        this.totalInsumos = totalInsumos;
        this.totalProductos = totalProductos;
        this.alertas = List.copyOf(alertas);
        this.ultimosMovimientos = List.copyOf(ultimosMovimientos);
        this.unidadesVendidasHoy = unidadesVendidasHoy;
        this.montoVendidoHoy = montoVendidoHoy;
        this.masVendidos = List.copyOf(masVendidos);
        this.agotados = List.copyOf(agotados);
    }

    public int getTotalInsumos() {
        return totalInsumos;
    }

    public int getTotalProductos() {
        return totalProductos;
    }

    public List<InsumoDTO> getAlertas() {
        return alertas;
    }

    public int getCantidadAlertas() {
        return alertas.size();
    }

    public List<MovimientoDTO> getUltimosMovimientos() {
        return ultimosMovimientos;
    }

    /** Galletas vendidas en el día (movimientos VENTA de productos). */
    public BigDecimal getUnidadesVendidasHoy() {
        return unidadesVendidasHoy;
    }

    /** Monto vendido en el día, a precio de venta actual de cada sabor. */
    public BigDecimal getMontoVendidoHoy() {
        return montoVendidoHoy;
    }

    public List<VentaSaborDTO> getMasVendidos() {
        return masVendidos;
    }

    public List<ProductoDTO> getAgotados() {
        return agotados;
    }

    /** Insumos bajo su mínimo más sabores agotados: lo que necesita atención. */
    public int getCantidadAtencion() {
        return alertas.size() + agotados.size();
    }
}
