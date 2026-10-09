package pe.com.dpaocookies.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import pe.com.dpaocookies.util.Formato;

/** Expone {@link Formato} a las vistas: {@code #{formato.monto(x)}}. */
@ManagedBean(name = "formato")
@ApplicationScoped
public class FormatoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public String monto(BigDecimal valor) {
        return Formato.monto(valor);
    }

    public String entero(BigDecimal valor) {
        return Formato.entero(valor);
    }

    public String decimales(BigDecimal valor) {
        return Formato.decimales(valor);
    }

    public String conSigno(BigDecimal valor) {
        return Formato.conSigno(valor);
    }

    public String unidades(BigDecimal valor) {
        return Formato.unidades(valor);
    }

    public String enumerar(List<String> elementos) {
        return Formato.enumerar(elementos);
    }
}
