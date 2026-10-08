package pe.com.dpaocookies.exception;

/**
 * Error cuyo mensaje puede mostrarse al usuario. Los detalles técnicos viajan
 * solo en la causa y se registran en el servidor.
 */
public abstract class AplicacionException extends RuntimeException {

    protected AplicacionException(String mensaje) {
        super(mensaje);
    }

    protected AplicacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
