package kore.backend.service.policy;

import kore.backend.dto.CategoriaRequestDTO;
import kore.backend.model.Categoria;
import kore.backend.repository.CategoriaRepository;
import org.springframework.stereotype.Component;

@Component
public class CategoriaPolicy {
    private final CategoriaRepository categoriaRepository;

    public CategoriaPolicy(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public void validarRegistro(CategoriaRequestDTO dto, Long fkUsuario) {
        if (dto == null) {
            throw new IllegalArgumentException("Categoria inválida");
        }

        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }

        if (fkUsuario == null) {
            throw new IllegalArgumentException("Usuário da categoria é obrigatório");
        }
    }

    public void validarAtualizacao(Categoria categoria, CategoriaRequestDTO dto) {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria não encontrada");
        }

        if (dto == null || dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
    }
}
