package kore.backend.service.validation;

import kore.backend.model.Categoria;

import java.util.List;

public interface CategoriaValidationService {
    Categoria obterCategoria(Long idCategoria);
    Categoria obterCategoriaDoUsuario(Long idCategoria, Long fkUsuario);
    List<Categoria> obterCategoriasPorIdDoUsuario(Long fkUsuario);
}
