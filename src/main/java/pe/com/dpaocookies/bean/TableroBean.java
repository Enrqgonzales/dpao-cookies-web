package pe.com.dpaocookies.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import pe.com.dpaocookies.dto.ResumenTableroDTO;
import pe.com.dpaocookies.exception.AplicacionException;
import pe.com.dpaocookies.facade.InventarioFacade;
import pe.com.dpaocookies.util.Formato;

/** Tablero: lo vendido hoy y lo que necesita atención. */
@ManagedBean(name = "tablero")
@ViewScoped
public class TableroBean extends BeanBase {

    private static final long serialVersionUID = 1L;
    /** Filas de «Atención» que caben sin desplazarse (computadora y celular); el resto se resume en una línea. */
    static final int FILAS_ATENCION = 6;
    static final int FILAS_ATENCION_MOVIL = 4;

    private ResumenTableroDTO resumen;
    /** Panel visible en el celular: {@code vendidos} o {@code atencion}. */
    private String pestana = "vendidos";

    public TableroBean() {
    }

    TableroBean(InventarioFacade facade, Vista vista) {
        super(facade, vista);
    }

    public ResumenTableroDTO getResumen() {
        if (resumen == null) {
            try {
                resumen = facade().obtenerResumen();
            } catch (AplicacionException exception) {
                vista().error(exception.getMessage());
                resumen = new ResumenTableroDTO(0, 0, List.of(), List.of());
            }
        }
        return resumen;
    }

    /** Insumos por reponer y luego sabores agotados, hasta {@link #FILAS_ATENCION} filas. */
    public List<Atencion> getAtencion() {
        List<Atencion> filas = new ArrayList<>();
        getResumen().getAlertas().forEach(a -> filas.add(new Atencion(a.getNombre(),
                Formato.monto(a.getStockActual()) + " de " + Formato.monto(a.getStockMinimo()) + " " + a.getUnidadMedida(), false)));
        getResumen().getAgotados().forEach(p -> filas.add(new Atencion(p.getNombre(), "Agotado", true)));
        return filas.size() > FILAS_ATENCION ? filas.subList(0, FILAS_ATENCION) : filas;
    }

    /** Cuántas filas de «Atención» no se muestran. */
    public int getAtencionRestante() {
        return Math.max(0, getResumen().getCantidadAtencion() - FILAS_ATENCION);
    }

    /** Cuántas filas de «Atención» no se muestran en el celular. */
    public int getAtencionRestanteMovil() {
        return Math.max(0, getResumen().getCantidadAtencion() - FILAS_ATENCION_MOVIL);
    }

    public boolean isPestanaAtencion() {
        return "atencion".equals(pestana);
    }

    public String getPestana() {
        return pestana;
    }

    public void setPestana(String pestana) {
        this.pestana = "atencion".equals(pestana) ? "atencion" : "vendidos";
    }

    /** Una fila del panel «Atención». */
    public static class Atencion implements Serializable {

        private static final long serialVersionUID = 1L;
        private final String nombre;
        private final String detalle;
        private final boolean agotado;

        public Atencion(String nombre, String detalle, boolean agotado) {
            this.nombre = nombre;
            this.detalle = detalle;
            this.agotado = agotado;
        }

        public String getNombre() {
            return nombre;
        }

        public String getDetalle() {
            return detalle;
        }

        public boolean isAgotado() {
            return agotado;
        }
    }
}
