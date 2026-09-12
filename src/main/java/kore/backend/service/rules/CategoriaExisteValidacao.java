package kore.backend.service.rules;

import jakarta.persistence.EntityExistsException;
import kore.backend.model.Categoria;
import kore.backend.repository.CategoriaRepository;
import org.springframework.stereotype.Component;

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
}
