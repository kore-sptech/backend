package kore.backend.service;

import jakarta.transaction.Transactional;
import kore.backend.dto.CategoriaRequestDTO;
import kore.backend.model.Categoria;
import kore.backend.repository.CategoriaRepository;
import kore.backend.service.policy.CategoriaPolicy;
import kore.backend.service.validation.CategoriaValidationService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final CategoriaValidationService categoriaValidationService;
    private final CategoriaPolicy categoriaPolicy;

    public CategoriaService(CategoriaRepository categoriaRepository,
                           CategoriaValidationService categoriaValidationService,
                           CategoriaPolicy categoriaPolicy) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaValidationService = categoriaValidationService;
        this.categoriaPolicy = categoriaPolicy;
    }

    @Transactional
    public Categoria adicionarCategoria(CategoriaRequestDTO dto, Long fkUsuario){
        categoriaPolicy.validarRegistro(dto, fkUsuario);
        Categoria categoria = new Categoria(
                dto.nome(),
                dto.descricao(),
                fkUsuario
        );
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarCategorias(Long fkUsuario){
      return categoriaValidationService.obterCategoriasPorIdDoUsuario(fkUsuario);
    }

    public Categoria listarCategoria(Long idCategoria, Long fkUsuario){
        return categoriaValidationService.obterCategoriaDoUsuario(idCategoria, fkUsuario);
    }

    @Transactional
    public Categoria atualizarCategoria(CategoriaRequestDTO dto,Long idCategoria, Long fkUsuario){
        Categoria c = listarCategoria(idCategoria, fkUsuario);
        categoriaPolicy.validarAtualizacao(c, dto);
        c.atualizar(dto.nome(), dto.descricao());
        return categoriaRepository.save(c);
    }

    @Transactional
    public void removerCategoria(Long id){
        categoriaRepository.delete(categoriaValidationService.obterCategoria(id));
    }
}
