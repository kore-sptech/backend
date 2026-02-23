package handler;

import exception.CredencialExistenteException;
import exception.RecursoNaoEncontradoException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExeptionHandler {
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<?> handleNotFound() {
        return ResponseEntity.notFound().build();// 404
    }
    @ExceptionHandler(CredencialExistenteException.class)
    public ResponseEntity<?> handleExistente(){
        return ResponseEntity.status(409).build(); // conflict
    }

}
