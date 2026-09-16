package kore.backend.exception;

public class AgendamentoNaoEncontradoException extends RuntimeException {

    public AgendamentoNaoEncontradoException() {
        super("Agendamento não encontrado");
    }

    public AgendamentoNaoEncontradoException(String message) {
        super(message);
    }

    public AgendamentoNaoEncontradoException(Long id) {
        super("Agendamento não encontrado com id: " + id);
    }
}
