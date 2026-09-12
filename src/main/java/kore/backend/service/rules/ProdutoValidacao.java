package kore.backend.service.rules;

import kore.backend.model.Produto;

public interface ProdutoValidacao {
    Produto validarEBuscar(Long idProduto, Long fkUsuario);
}
