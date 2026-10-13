package pe.com.dpaocookies.bean;

import java.io.Serializable;
import pe.com.dpaocookies.facade.InventarioFacade;

/**
 * Base de los beans de pantalla: la fachada y la vista se crean al primer uso y no se
 * serializan con el estado de la vista (se recrean si la sesión se restaura).
 */
public abstract class BeanBase implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient InventarioFacade facade;
    private transient Vista vista;

    protected BeanBase() {
    }

    /** Para pruebas: fachada y vista falsas. */
    protected BeanBase(InventarioFacade facade, Vista vista) {
        this.facade = facade;
        this.vista = vista;
    }

    protected InventarioFacade facade() {
        if (facade == null) {
            facade = new InventarioFacade();
        }
        return facade;
    }

    protected Vista vista() {
        if (vista == null) {
            vista = new VistaJsf();
        }
        return vista;
    }

    /** Redirección tras un POST correcto (Post/Redirect/Get). */
    protected static String redirigir(String pagina) {
        return pagina + "?faces-redirect=true";
    }
}
