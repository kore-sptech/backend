package kore.backend.handler;

import kore.backend.exception.AgendamentoNaoEncontradoException;
import kore.backend.exception.CredencialExistenteException;
import kore.backend.exception.RecursoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExeptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExeptionHandler.class);

        // ── 404 — Recurso não encontrado ───────────────────────────────────────

        @ExceptionHandler(RecursoNaoEncontradoException.class)
        public ResponseEntity<ErrorResponse> handleRecursoNaoEncontrado(
                        RecursoNaoEncontradoException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request));
        }

        // ── 404 — Agendamento não encontrado ───────────────────────────────────

        @ExceptionHandler(AgendamentoNaoEncontradoException.class)
        public ResponseEntity<ErrorResponse> handleAgendamentoNaoEncontrado(
                        AgendamentoNaoEncontradoException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request));
        }

        // ── 409 — Credencial já existente ──────────────────────────────────────

        @ExceptionHandler(CredencialExistenteException.class)
        public ResponseEntity<ErrorResponse> handleCredencialExistente(
                        CredencialExistenteException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(HttpStatus.CONFLICT,
                                                ex.getMessage(), request));
        }

        // ── 401 — Credenciais inválidas ────────────────────────────────────────

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUsernameNotFound(
                        UsernameNotFoundException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", request));
        }

        // ── 400 — Argumento inválido ───────────────────────────────────────────

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(
                        IllegalArgumentException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
        }

        // ── 400 — Validação Bean (Jakarta) ─────────────────────────────────────

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {
                String mensagem = ex.getBindingResult().getFieldErrors().stream()
                                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                .reduce((a, b) -> a + "; " + b)
                                .orElse("Dados inválidos");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, mensagem, request));
        }

        // ── 500 — Erro de banco ────────────────────────────────────────────────

        @ExceptionHandler(DataAccessException.class)
        public ResponseEntity<ErrorResponse> handleDataAccess(
                        DataAccessException ex, HttpServletRequest request) {
                log.error("Erro de acesso a dados: {}", ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Erro ao acessar os dados. Tente novamente mais tarde.", request));
        }

        // ── 500 — Fallback genérico ────────────────────────────────────────────

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ErrorResponse> handleRuntimeException(
                        RuntimeException ex, HttpServletRequest request) {
                log.error("Erro interno não tratado: {}", ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Erro interno no servidor. Tente novamente mais tarde.", request));
        }

        // ── 413 — Tamanho do arquivo excede o limite permitido ────────────────
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
                        MaxUploadSizeExceededException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .body(ErrorResponse.of(HttpStatus.PAYLOAD_TOO_LARGE,
                                                "O tamanho do arquivo excede o limite permitido.", request));
        }
}