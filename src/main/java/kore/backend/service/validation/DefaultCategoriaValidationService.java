package kore.backend.service.validation;

import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Categoria;
import kore.backend.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DefaultCategoriaValidationService implements CategoriaValidationService {
    private final CategoriaRepository categoriaRepository;

    public DefaultCategoriaValidationService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public Categoria obterCategoria(Long idCategoria) {
        return categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada", idCategoria));
    }

    @Override
    public Categoria obterCategoriaDoUsuario(Long idCategoria, Long fkUsuario) {
        return categoriaRepository.buscarCategoriaPorIdDaCategoriaEDoUsuario(fkUsuario, idCategoria)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada", idCategoria));
    }

    @Override
    public List<Categoria> obterCategoriasPorIdDoUsuario(Long fkUsuario) {
        return categoriaRepository.buscarPorIdDoUsuario(fkUsuario)
                .orElse(Collections.emptyList());
    }
}
