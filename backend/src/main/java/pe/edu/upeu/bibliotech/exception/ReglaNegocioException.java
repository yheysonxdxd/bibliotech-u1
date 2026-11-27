package pe.edu.upeu.bibliotech.exception;

import java.util.List;

/** Se lanza cuando los datos violan una regla de negocio (HTTP 400). */
public class ReglaNegocioException extends RuntimeException {
    private final List<String> errores;

    public ReglaNegocioException(String mensaje) {
        this(List.of(mensaje));
    }

    public ReglaNegocioException(List<String> errores) {
        super(String.join("; ", errores));
        this.errores = List.copyOf(errores);
    }

    public List<String> getErrores() { return errores; }
}
