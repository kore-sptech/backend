package kore.backend.service.rules;

import kore.backend.model.Categoria;

import java.util.List;

public interface CategoriaValidacao {
    Categoria obterCategoria(Long idCategoria);
    Categoria obterCategoriaDoUsuario(Long idCategoria, Long fkUsuario);
    List<Categoria> obterCategoriasPorIdDoUsuario(Long fkUsuario);
}
