package kore.backend.service.rules;

import jakarta.persistence.EntityExistsException;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Categoria;
import kore.backend.repository.CategoriaRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CategoriaExisteValidacao implements CategoriaValidacao{
    private final CategoriaRepository categoriaRepository;

    public CategoriaExisteValidacao(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }


    @Override
    public Categoria obterCategoria(Long idCategoria) {
        return categoriaRepository.findById(idCategoria).orElseThrow(
                () -> new EntityExistsException("Categoria não existe. Id da categoria: " +idCategoria)
        );
    }

    @Override
    public Categoria obterCategoriaDoUsuario(Long idCategoria, Long fkUsuario) {
        return (Categoria) categoriaRepository.buscarCategoriaPorIdDaCategoriaEDoUsuario(fkUsuario, idCategoria)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada", idCategoria));
    }

    @Override
    public List<Categoria> obterCategoriaPorIdDoUsuario(Long fkUsuario) {
        return categoriaRepository.buscarPorIdDoUsuario(fkUsuario)
                .orElse(Collections.emptyList());
    }
}
