package madstodolist.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
public class UsuarioNoAutorizadoException extends RuntimeException {
    public UsuarioNoAutorizadoException() {
        super("No autorizado: no tienes permiso para acceder a esta página.");
    }
}
