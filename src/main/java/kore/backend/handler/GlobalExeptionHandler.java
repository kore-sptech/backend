package kore.backend.handler;

import kore.backend.exception.CredencialExistenteException;
import kore.backend.exception.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExeptionHandler {
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<?> handleNotFound(RecursoNaoEncontradoException ex) {
        return ResponseEntity.notFound().build();// 404
    }

    @ExceptionHandler(CredencialExistenteException.class)
    public ResponseEntity<Map<String, Object>> handleCredencialExistente(CredencialExistenteException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("code", "EMAIL_ALREADY_EXISTS");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body); // conflict
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageErrorResponse> handlerIllegalArgumentException(
            IllegalArgumentException ex
    ){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageErrorResponse(ex.getMessage()));
    }


    record MessageErrorResponse(
            String message
    ){}
}
