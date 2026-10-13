package pe.com.dpaocookies.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import pe.com.dpaocookies.dto.InsumoDTO;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.dto.SolicitudMovimientoDTO;
import pe.com.dpaocookies.exception.AplicacionException;
import pe.com.dpaocookies.exception.StockInsuficienteException;
import pe.com.dpaocookies.facade.InventarioFacade;
import pe.com.dpaocookies.util.Formato;

/**
 * Kardex: movimientos agrupados por día, filtro por tipo y fechas, y hoja de nuevo movimiento.
 * El saldo, el signo y la transacción los decide la fachada; aquí solo se arma la solicitud.
 */
@ManagedBean(name = "kardex")
@ViewScoped
public class KardexBean extends BeanBase {

    private static final long serialVersionUID = 1L;
    static final String HOJA_NUEVO = "nuevo";
    static final String HOJA_FILTRO = "filtro";
    static final String ID_HOJA = "formulario";
    static final String ID_CANTIDAD = "formulario:cantidad";
    static final String ID_FILTRO = "filtro";
    /** Filas que se traen como máximo; la vista las reparte en páginas. */
    static final int MAXIMO_FILAS = 200;
    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("EEE d MMM", ES_PE);
    /** Tipos del Kardex y su nombre en pantalla (el valor es el que entiende la fachada). */
    private static final Map<String, String> TIPOS = new LinkedHashMap<>();

    static {
        TIPOS.put("ENTRADA", "Entrada");
        TIPOS.put("SALIDA", "Salida");
        TIPOS.put("VENTA", "Venta");
        TIPOS.put("MERMA", "Merma");
        TIPOS.put("AJUSTE", "Ajuste");
    }

    private List<Dia> dias;
    private int totalFilas;
    private List<SelectItem> items;
    private String hoja;
    private String tipo;
    private LocalDate desde;
    private LocalDate hasta;
    private final transient Hoy hoy;

    // Hoja de nuevo movimiento
    private String item;
    private String tipoNuevo = "ENTRADA";
    private BigDecimal cantidad;
    private String motivo;

    /** Reloj de "hoy" en Lima, sustituible en pruebas. */
    @FunctionalInterface
    interface Hoy {
        LocalDate fecha();
    }

    public KardexBean() {
        this.hoy = null;
    }

    KardexBean(InventarioFacade facade, Vista vista, Hoy hoy) {
        super(facade, vista);
        this.hoy = hoy;
    }

    private LocalDate hoy() {
        return hoy != null ? hoy.fecha() : LocalDate.now(InventarioFacade.ZONA);
    }

    public String registrar() {
        SolicitudMovimientoDTO solicitud = new SolicitudMovimientoDTO();
        int separador = item == null ? -1 : item.indexOf(':');
        if (separador > 0) {
            solicitud.setClaseItem(item.substring(0, separador));
            try {
                solicitud.setIdItem(Integer.parseInt(item.substring(separador + 1)));
            } catch (NumberFormatException exception) {
                solicitud.setIdItem(0);
            }
        }
        solicitud.setTipoMovimiento(tipoNuevo);
        solicitud.setCantidad(cantidad);
        solicitud.setMotivo(motivo);

        Optional<MovimientoDTO> movimiento;
        try {
            movimiento = facade().registrarMovimiento(solicitud);
        } catch (StockInsuficienteException exception) {
            vista().errorEn(ID_CANTIDAD, exception.getMessage());
            return null;
        } catch (AplicacionException exception) {
            vista().errorEn(ID_HOJA, exception.getMessage());
            return null;
        }
        vista().exito(movimiento
                .map(m -> etiqueta(m.getTipoMovimiento())
                        + ("AJUSTE".equals(m.getTipoMovimiento()) ? " registrado · " : " registrada · ")
                        + m.getNombreItem() + " "
                        + Formato.monto(m.getStockAnterior()) + " → " + Formato.monto(m.getStockPosterior()))
                .orElse("Sin cambios · el stock contado es igual al actual"));
        return redirigir("kardex");
    }

    /** Aplica el filtro por la dirección para que se pueda recargar y compartir. */
    public String filtrar() {
        try {
            facade().consultarKardex(tipo, desde, hasta, 1);   // la fachada decide si el rango es válido
        } catch (AplicacionException exception) {
            vista().errorEn(ID_FILTRO, exception.getMessage());
            return null;
        }
        hoja = null;
        return "kardex?faces-redirect=true&includeViewParams=true";
    }

    // ------------------------------------------------------------ Para la vista

    /** Movimientos agrupados por día de Lima, del más reciente al más antiguo. */
    public List<Dia> getDias() {
        if (dias == null) {
            List<MovimientoDTO> movimientos;
            try {
                movimientos = facade().consultarKardex(tipo, desde, hasta, MAXIMO_FILAS);
            } catch (AplicacionException exception) {
                vista().error(exception.getMessage());
                movimientos = List.of();
            }
            totalFilas = movimientos.size();
            Map<LocalDate, List<MovimientoDTO>> porDia = new LinkedHashMap<>();
            for (MovimientoDTO m : movimientos) {
                porDia.computeIfAbsent(dia(m.getFechaRegistro()), k -> new ArrayList<>()).add(m);
            }
            dias = new ArrayList<>();
            for (Map.Entry<LocalDate, List<MovimientoDTO>> e : porDia.entrySet()) {
                dias.add(new Dia(nombreDia(e.getKey()), e.getValue()));
            }
        }
        return dias;
    }

    public int getTotalFilas() {
        getDias();
        return totalFilas;
    }

    String nombreDia(LocalDate dia) {
        if (dia == null) {
            return "Sin fecha";
        }
        LocalDate actual = hoy();
        if (dia.equals(actual)) {
            return "Hoy";
        }
        if (dia.equals(actual.minusDays(1))) {
            return "Ayer";
        }
        String texto = FECHA_CORTA.format(dia).replace(".", "");
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static LocalDate dia(Date fecha) {
        return fecha == null ? null
                : java.time.Instant.ofEpochMilli(fecha.getTime()).atZone(InventarioFacade.ZONA).toLocalDate();
    }

    /** Ítems para la hoja: sabores e insumos en dos grupos; el valor es {@code CLASE:id}. */
    public List<SelectItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
            try {
                List<ProductoDTO> productos = facade().listarProductos();
                List<InsumoDTO> insumos = facade().listarInsumos();
                SelectItemGroup sabores = new SelectItemGroup("Sabores");
                sabores.setSelectItems(productos.stream()
                        .map(p -> new SelectItem("PRODUCTO:" + p.getIdProducto(),
                                p.getNombre() + " · " + Formato.unidades(p.getStockActual()) + " en vitrina"))
                        .toArray(SelectItem[]::new));
                SelectItemGroup cocina = new SelectItemGroup("Insumos");
                cocina.setSelectItems(insumos.stream()
                        .map(i -> new SelectItem("INSUMO:" + i.getIdInsumo(),
                                i.getNombre() + " · " + Formato.monto(i.getStockActual()) + " " + i.getUnidadMedida()))
                        .toArray(SelectItem[]::new));
                if (!productos.isEmpty()) {
                    items.add(sabores);
                }
                if (!insumos.isEmpty()) {
                    items.add(cocina);
                }
            } catch (AplicacionException exception) {
                vista().error(exception.getMessage());
            }
        }
        return items;
    }

    public List<SelectItem> getTipos() {
        return TIPOS.entrySet().stream().map(e -> new SelectItem(e.getKey(), e.getValue())).toList();
    }

    public String etiqueta(String tipoMovimiento) {
        return TIPOS.getOrDefault(tipoMovimiento, tipoMovimiento);
    }

    public boolean isFiltrado() {
        return (tipo != null && !tipo.isBlank()) || desde != null || hasta != null;
    }

    public boolean isHojaNuevo() {
        return HOJA_NUEVO.equals(hoja);
    }

    public boolean isHojaFiltro() {
        return HOJA_FILTRO.equals(hoja);
    }

    public String getHoja() {
        return hoja;
    }

    public void setHoja(String hoja) {
        this.hoja = hoja;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo == null || !TIPOS.containsKey(tipo) ? null : tipo;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getTipoNuevo() {
        return tipoNuevo;
    }

    public void setTipoNuevo(String tipoNuevo) {
        this.tipoNuevo = tipoNuevo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    /** Un día del Kardex con sus movimientos. */
    public static class Dia implements Serializable {

        private static final long serialVersionUID = 1L;
        private final String nombre;
        private final List<MovimientoDTO> movimientos;

        public Dia(String nombre, List<MovimientoDTO> movimientos) {
            this.nombre = nombre;
            this.movimientos = movimientos;
        }

        public String getNombre() {
            return nombre;
        }

        public List<MovimientoDTO> getMovimientos() {
            return movimientos;
        }
    }
}
