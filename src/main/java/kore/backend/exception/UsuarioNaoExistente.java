package kore.backend.exception;

public class UsuarioNaoExistente extends RuntimeException {

    private Object idUsuario;
    public UsuarioNaoExistente(Object idUsuario) {
        super("Usuario inexistente, id: " + idUsuario);
        this.idUsuario = idUsuario;
    }
}
