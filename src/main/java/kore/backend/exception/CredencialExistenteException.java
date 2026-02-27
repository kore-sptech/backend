package kore.backend.exception;

import lombok.Getter;

@Getter
public class CredencialExistenteException extends RuntimeException {
    private final String recurso;
    private final String campo;
    private final Object valor;

    public CredencialExistenteException(String recurso, String email) {
        this.recurso = recurso;
        this.campo = "email";
        this.valor = email;
    }


}
