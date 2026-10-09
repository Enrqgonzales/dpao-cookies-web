package pe.com.dpaocookies.bean;

import java.io.Serializable;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/** {@link Vista} sobre el {@code FacesContext} de la petición en curso. */
public class VistaJsf implements Vista, Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MINUTOS_SESION = 30;

    private FacesContext contexto() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    public void exito(String mensaje) {
        FacesContext contexto = contexto();
        contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, mensaje, null));
        contexto.getExternalContext().getFlash().setKeepMessages(true);
    }

    @Override
    public void error(String mensaje) {
        contexto().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
    }

    @Override
    public void errorEn(String idCliente, String mensaje) {
        contexto().addMessage(idCliente, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
    }

    @Override
    public void iniciarSesion(String usuario, String local) {
        ExternalContext externo = contexto().getExternalContext();
        HttpServletRequest peticion = (HttpServletRequest) externo.getRequest();
        peticion.changeSessionId();   // evita la fijación de sesión
        Map<String, Object> sesion = externo.getSessionMap();
        sesion.put("usuario", usuario);
        sesion.put("local", local);
        peticion.getSession().setMaxInactiveInterval(MINUTOS_SESION * 60);
    }

    @Override
    public void cerrarSesion() {
        contexto().getExternalContext().invalidateSession();
    }

    @Override
    public String usuarioActual() {
        Object usuario = contexto().getExternalContext().getSessionMap().get("usuario");
        return usuario == null ? null : usuario.toString();
    }
}
