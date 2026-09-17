package kore.backend.exception;

public class UsuarioNaoEncontradoPeloProduto extends RuntimeException {

    private final Object idProduto;
    private final Object idUsuario;

    public UsuarioNaoEncontradoPeloProduto(String idProduto, String idUsuario) {
        super("Id usuario não encontrado: " + idUsuario + " do id do produto: " +idProduto);
        this.idProduto = idProduto;
        this.idUsuario = idUsuario;
    }
}
