package pe.com.dpaocookies.bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import pe.com.dpaocookies.dto.CategoriaDTO;
import pe.com.dpaocookies.dto.PackDTO;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.exception.AplicacionException;
import pe.com.dpaocookies.facade.InventarioFacade;

/** Sabores (mosaicos por categoría, hoja de alta y edición) y packs de delivery. */
@ManagedBean(name = "productos")
@ViewScoped
public class ProductosBean extends BeanBase {

    private static final long serialVersionUID = 1L;
    static final String HOJA_NUEVO = "nuevo";
    static final String HOJA_EDITAR = "editar";
    static final String ID_HOJA = "formulario";

    private List<ProductoDTO> lista;
    private List<PackDTO> packs;
    private List<CategoriaDTO> categorias;
    private String pestana = "sabores";
    private int categoria;
    private String hoja;
    private int id;
    private ProductoDTO sabor = new ProductoDTO();

    public ProductosBean() {
    }

    ProductosBean(InventarioFacade facade, Vista vista) {
        super(facade, vista);
    }

    /** {@code f:viewAction}: carga el sabor a editar o propone la categoría del filtro. */
    public void preparar() {
        if (HOJA_EDITAR.equals(hoja)) {
            ProductoDTO actual = getLista().stream().filter(p -> p.getIdProducto() == id).findFirst().orElse(null);
            if (actual == null) {
                hoja = null;
                return;
            }
            if (sabor.getIdProducto() == 0) {
                sabor = copia(actual);
            }
        } else if (HOJA_NUEVO.equals(hoja) && sabor.getIdCategoria() == 0) {
            sabor.setIdCategoria(categoria != 0 ? categoria
                    : getCategoriasConSabores().stream().map(c -> c.getCategoria().getIdCategoria()).findFirst().orElse(0));
        }
    }

    public String guardar() {
        boolean esNuevo = sabor.getIdProducto() == 0;
        try {
            facade().guardarProducto(sabor);
        } catch (AplicacionException exception) {
            vista().errorEn(ID_HOJA, exception.getMessage());
            return null;
        }
        vista().exito((esNuevo ? "Sabor registrado · " : "Sabor guardado · ") + sabor.getNombre().trim());
        return redirigir("productos");
    }

    // ------------------------------------------------------------ Para la vista

    public List<ProductoDTO> getLista() {
        if (lista == null) {
            try {
                lista = facade().listarProductos();
            } catch (AplicacionException exception) {
                vista().error(exception.getMessage());
                lista = List.of();
            }
        }
        return lista;
    }

    /** Sabores del filtro de categoría elegido (0 = todos). */
    public List<ProductoDTO> getSabores() {
        return categoria == 0 ? getLista()
                : getLista().stream().filter(p -> p.getIdCategoria() == categoria).toList();
    }

    /** Categorías que tienen sabores, con cuántos tiene cada una (para los chips). */
    public List<Cuenta> getCategoriasConSabores() {
        Map<Integer, Integer> cuentas = new LinkedHashMap<>();
        for (ProductoDTO p : getLista()) {
            cuentas.merge(p.getIdCategoria(), 1, Integer::sum);
        }
        List<Cuenta> resultado = new ArrayList<>();
        for (CategoriaDTO c : getCategorias()) {
            if (cuentas.containsKey(c.getIdCategoria())) {
                resultado.add(new Cuenta(c, cuentas.get(c.getIdCategoria())));
            }
        }
        return resultado;
    }

    /** Categorías ofrecidas en la hoja: las que ya tienen sabores; si no hay ninguna, todas. */
    public List<CategoriaDTO> getCategoriasDeSabor() {
        List<CategoriaDTO> conSabores = getCategoriasConSabores().stream().map(Cuenta::getCategoria).toList();
        return conSabores.isEmpty() ? getCategorias() : conSabores;
    }

    public String getNombreCategoria() {
        return getCategorias().stream().filter(c -> c.getIdCategoria() == categoria)
                .map(CategoriaDTO::getNombre).findFirst().orElse("");
    }

    public List<PackDTO> getPacks() {
        if (packs == null) {
            try {
                packs = facade().listarPacks();
            } catch (AplicacionException exception) {
                vista().error(exception.getMessage());
                packs = List.of();
            }
        }
        return packs;
    }

    private List<CategoriaDTO> getCategorias() {
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

    public boolean isPestanaPacks() {
        return "packs".equals(pestana);
    }

    public boolean isHojaAbierta() {
        return !isPestanaPacks() && (HOJA_NUEVO.equals(hoja) || HOJA_EDITAR.equals(hoja));
    }

    public boolean isEditando() {
        return sabor.getIdProducto() != 0;
    }

    public String getPestana() {
        return pestana;
    }

    public void setPestana(String pestana) {
        this.pestana = "packs".equals(pestana) ? "packs" : "sabores";
    }

    public int getCategoria() {
        return categoria;
    }

    public void setCategoria(int categoria) {
        this.categoria = Math.max(categoria, 0);
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

    public ProductoDTO getSabor() {
        return sabor;
    }

    private static ProductoDTO copia(ProductoDTO origen) {
        ProductoDTO destino = new ProductoDTO();
        destino.setIdProducto(origen.getIdProducto());
        destino.setCodigo(origen.getCodigo());
        destino.setNombre(origen.getNombre());
        destino.setPrecioVenta(origen.getPrecioVenta());
        destino.setStockActual(origen.getStockActual());
        destino.setIdCategoria(origen.getIdCategoria());
        destino.setNombreCategoria(origen.getNombreCategoria());
        return destino;
    }

    /** Categoría con su número de sabores. */
    public static class Cuenta implements java.io.Serializable {

        private static final long serialVersionUID = 1L;
        private final CategoriaDTO categoria;
        private final int sabores;

        public Cuenta(CategoriaDTO categoria, int sabores) {
            this.categoria = categoria;
            this.sabores = sabores;
        }

        public CategoriaDTO getCategoria() {
            return categoria;
        }

        public int getSabores() {
            return sabores;
        }
    }
}
