package kore.backend.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    private final String recurso;
    private final String campo;
    private final Object valor;

    public RecursoNaoEncontradoException(String recurso, Long id) {
        this.recurso = recurso;
        this.campo = "id";
        this.valor = id;
    }
}
