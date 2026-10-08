package pe.com.dpaocookies.exception;

/** Regla de negocio o dato de entrada inválido. */
public class NegocioException extends AplicacionException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
