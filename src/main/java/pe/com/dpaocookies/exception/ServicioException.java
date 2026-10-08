package pe.com.dpaocookies.exception;

/** Falla técnica (base de datos no disponible, error SQL inesperado). */
public class ServicioException extends AplicacionException {

    public ServicioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
