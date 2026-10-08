package pe.com.dpaocookies.dto;

import java.io.Serializable;
import java.util.List;

/** Indicadores del tablero reunidos por la fachada en una sola llamada. */
public class ResumenTableroDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int totalInsumos;
    private final int totalProductos;
    private final List<InsumoDTO> alertas;
    private final List<MovimientoDTO> ultimosMovimientos;

    public ResumenTableroDTO(int totalInsumos, int totalProductos, List<InsumoDTO> alertas,
            List<MovimientoDTO> ultimosMovimientos) {
        this.totalInsumos = totalInsumos;
        this.totalProductos = totalProductos;
        this.alertas = List.copyOf(alertas);
        this.ultimosMovimientos = List.copyOf(ultimosMovimientos);
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
}
