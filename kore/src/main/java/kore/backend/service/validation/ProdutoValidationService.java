package kore.backend.service.validation;

import kore.backend.model.Produto;

public interface ProdutoValidationService {
    void validarUsuario(Long fkUsuario);
    Produto validarProdutoDoUsuario(Long idProduto, Long fkUsuario);
}
