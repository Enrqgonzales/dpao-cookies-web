package pe.com.dpaocookies.bean;

/**
 * Lo que un bean necesita de la vista JSF: mensajes y sesión. Separarlo de {@code FacesContext}
 * permite probar los beans sin contenedor.
 */
public interface Vista {

    /** Mensaje de éxito que sobrevive a la redirección (Post/Redirect/Get). */
    void exito(String mensaje);

    /** Error general de la página actual. */
    void error(String mensaje);

    /** Error asociado a un componente (id de cliente, por ejemplo {@code formulario:cantidad}). */
    void errorEn(String idCliente, String mensaje);

    /** Renueva el id de sesión y guarda quién ingresó y en qué local. */
    void iniciarSesion(String usuario, String local);

    void cerrarSesion();

    /** Usuario de la sesión actual, o {@code null}. */
    String usuarioActual();
}
