package pe.com.dpaocookies.bean;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Datos de quien ingresó, vivos mientras dure la sesión: usuario y local de trabajo.
 * La plantilla los muestra con {@code #{sesion.local}}; {@link AccesoBean} los fija al ingresar.
 */
@ManagedBean(name = "sesion")
@SessionScoped
public class SesionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String usuario;
    private String local;

    /** Registra el ingreso; el control de acceso de las páginas lo hace {@code SeguridadFilter}. */
    public void abrir(String usuario, String local) {
        this.usuario = usuario;
        this.local = local;
    }

    public boolean isIngresado() {
        return usuario != null;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getLocal() {
        return local;
    }
}
