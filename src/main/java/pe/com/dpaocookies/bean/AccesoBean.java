package pe.com.dpaocookies.bean;

import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import pe.com.dpaocookies.util.Autenticacion;

/**
 * Ingreso y salida. La cuenta se valida contra la configuración del servidor, nunca contra el código.
 * Los resultados {@code ingreso} y {@code salida} se resuelven con las reglas de navegación de
 * {@code faces-config.xml} (ingreso correcto → tablero; salida → acceso).
 */
@ManagedBean(name = "acceso")
@RequestScoped
public class AccesoBean extends BeanBase {

    private static final long serialVersionUID = 1L;
    static final String ID_CLAVE = "acceso:clave";
    static final String INGRESO = "ingreso";
    static final String SALIDA = "salida";
    private static final List<String> LOCALES = List.of("Plaza Mayor", "Delivery");

    private transient Autenticacion autenticacion;
    /** Bean de sesión inyectado por JSF: guarda usuario y local mientras dure la sesión. */
    @ManagedProperty("#{sesion}")
    private SesionBean sesion;
    private String usuario;
    private String clave;
    private String local = LOCALES.get(0);

    public AccesoBean() {
    }

    AccesoBean(Autenticacion autenticacion, Vista vista, SesionBean sesion) {
        super(null, vista);
        this.autenticacion = autenticacion;
        this.sesion = sesion;
    }

    /** @return {@code ingreso} si la cuenta es válida; {@code null} para quedarse en el formulario */
    public String ingresar() {
        if (autenticacion == null) {
            autenticacion = new Autenticacion();
        }
        String elegido = LOCALES.contains(local) ? local : LOCALES.get(0);
        if (!autenticacion.valida(usuario, clave)) {
            clave = null;
            vista().errorEn(ID_CLAVE, "Usuario o contraseña incorrectos.");
            return null;
        }
        vista().iniciarSesion(usuario);
        sesion.abrir(usuario, elegido);
        return INGRESO;
    }

    public String salir() {
        vista().cerrarSesion();
        return SALIDA;
    }

    public void setSesion(SesionBean sesion) {
        this.sesion = sesion;
    }

    public List<String> getLocales() {
        return LOCALES;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario == null ? null : usuario.trim();
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }
}
