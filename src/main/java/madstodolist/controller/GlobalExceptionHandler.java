package madstodolist.controller;

import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNoAutorizadoException.class)
    public ResponseEntity<String> handleUsuarioNoAutorizado(UsuarioNoAutorizadoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("No autorizado: no tienes permiso para acceder a esta página.");
    }

    @ExceptionHandler(UsuarioNoLogeadoException.class)
    public ResponseEntity<String> handleUsuarioNoLogeado(UsuarioNoLogeadoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Debes iniciar sesión para acceder a esta página.");
    }
}
