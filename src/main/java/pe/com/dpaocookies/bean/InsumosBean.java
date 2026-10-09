package pe.com.dpaocookies.bean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import pe.com.dpaocookies.dto.CategoriaDTO;
import pe.com.dpaocookies.dto.InsumoDTO;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.exception.AplicacionException;
import pe.com.dpaocookies.facade.InventarioFacade;
import pe.com.dpaocookies.util.Formato;

/**
 * Insumos de cocina: tabla de existencias, hoja de alta y hoja de ajuste por conteo.
 * Las validaciones y el cálculo del ajuste son de la fachada; aquí solo se arma el DTO.
 */
@ManagedBean(name = "insumos")
@ViewScoped
public class InsumosBean extends BeanBase {

    private static final long serialVersionUID = 1L;
    static final String HOJA_NUEVO = "nuevo";
    static final String HOJA_AJUSTE = "ajuste";
    static final String ID_HOJA = "formulario";
    static final String ID_AJUSTE = "ajuste";
    private static final List<String> UNIDADES = List.of("kg", "g", "litro", "ml", "unidad");

    private List<InsumoDTO> lista;
    private List<CategoriaDTO> categorias;
    private String hoja;
    private int id;
    private InsumoDTO nuevo = new InsumoDTO();
    private BigDecimal stockContado;

    public InsumosBean() {
    }

    InsumosBean(InventarioFacade facade, Vista vista) {
        super(facade, vista);
    }

    /** {@code f:viewAction}: prepara la hoja pedida por la dirección. */
    public void preparar() {
        if (HOJA_NUEVO.equals(hoja) && nuevo.getIdCategoria() == 0) {
            nuevo.setUnidadMedida(UNIDADES.get(0));
            nuevo.setIdCategoria(getLista().stream().map(InsumoDTO::getIdCategoria).findFirst().orElse(0));
        } else if (HOJA_AJUSTE.equals(hoja)) {
            InsumoDTO elegido = getElegido();
            if (elegido == null) {
                hoja = null;
            } else if (stockContado == null) {
                stockContado = elegido.getStockActual();
            }
        }
    }

    public String registrar() {
        try {
            facade().registrarInsumo(nuevo);
        } catch (AplicacionException exception) {
            vista().errorEn(ID_HOJA, exception.getMessage());
            return null;
        }
        vista().exito("Insumo registrado · " + nuevo.getNombre().trim());
        return redirigir("insumos");
    }

    public String ajustar() {
        InsumoDTO elegido = getElegido();
        if (elegido == null) {
            vista().error("El insumo ya no existe.");
            return redirigir("insumos");
        }
        Optional<MovimientoDTO> movimiento;
        try {
            movimiento = facade().ajustarStockInsumo(elegido.getIdInsumo(), stockContado);
        } catch (AplicacionException exception) {
            vista().errorEn(ID_AJUSTE, exception.getMessage());
            return null;
        }
        vista().exito(movimiento
                .map(m -> "Ajuste registrado · " + elegido.getNombre() + " "
                        + Formato.monto(m.getStockAnterior()) + " → " + Formato.monto(m.getStockPosterior()))
                .orElse("Sin cambios · " + elegido.getNombre() + " ya tenía "
                        + Formato.monto(elegido.getStockActual())));
        return redirigir("insumos");
    }

    // ------------------------------------------------------------ Para la vista

    public List<InsumoDTO> getLista() {
        if (lista == null) {
            try {
                lista = facade().listarInsumos();
            } catch (AplicacionException exception) {
                vista().error(exception.getMessage());
                lista = List.of();
            }
        }
        return lista;
    }

    public List<CategoriaDTO> getCategorias() {
        if (categorias == null) {
            try {
                categorias = facade().listarCategorias();
            } catch (AplicacionException exception) {
                vista().error(exception.getMessage());
                categorias = List.of();
            }
        }
        return categorias;
    }

    public long getPorReponer() {
        return getLista().stream().filter(InsumoDTO::isBajoStock).count();
    }

    public InsumoDTO getElegido() {
        return getLista().stream().filter(i -> i.getIdInsumo() == id).findFirst().orElse(null);
    }

    public boolean isHojaNuevo() {
        return HOJA_NUEVO.equals(hoja);
    }

    public boolean isHojaAjuste() {
        return HOJA_AJUSTE.equals(hoja) && getElegido() != null;
    }

    public List<String> getUnidades() {
        return UNIDADES;
    }

    public String getHoja() {
        return hoja;
    }

    public void setHoja(String hoja) {
        this.hoja = hoja;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public InsumoDTO getNuevo() {
        return nuevo;
    }

    public BigDecimal getStockContado() {
        return stockContado;
    }

    public void setStockContado(BigDecimal stockContado) {
        this.stockContado = stockContado;
    }
}
