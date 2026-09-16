package kore.backend.exception;

public class AgendamentoNaoEncondradoException extends RuntimeException {

    public AgendamentoNaoEncondradoException() {
        super("Agendamento não encontrado");
    }

    public AgendamentoNaoEncondradoException(String message) {
        super(message);
    }

}
