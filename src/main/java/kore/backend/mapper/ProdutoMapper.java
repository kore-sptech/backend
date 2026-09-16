package kore.backend.mapper;

import kore.backend.dto.produto.ProdutoDTO;
import kore.backend.model.Produto;

public final class ProdutoMapper {
    private ProdutoMapper() {
    }

    public static Produto fromDto(ProdutoDTO produtoDTO) {
        Produto produto = new Produto();
        produto.setNome(produtoDTO.nome());
        produto.setDescricao(produtoDTO.descricao());
        produto.setPossuiValidade(produtoDTO.possuiValidade());
        produto.setQtdMinAlerta(produtoDTO.qtdMinAlerta());
        produto.setTipo(produtoDTO.tipo());
        produto.setFkUsuario(produtoDTO.usuario());
        return produto;
    }
}
