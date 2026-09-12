package kore.backend.service;

import jakarta.transaction.Transactional;
import kore.backend.dto.CategoriaRequestDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Categoria;
import kore.backend.repository.CategoriaRepository;
import kore.backend.service.rules.CategoriaExisteValidacao;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final CategoriaExisteValidacao categoriaExisteValidacao;

    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaExisteValidacao categoriaExisteValidacao) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaExisteValidacao = categoriaExisteValidacao;
    }
    @Transactional
    public Categoria adicionarCategoria(CategoriaRequestDTO dto, Long fkUsuario){
        Categoria categoria = new Categoria(
                dto.nome(),
                dto.descricao(),
                fkUsuario
        );
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarCategorias(Long fkUsuario){
      return categoriaExisteValidacao.obterCategoriasPorIdDoUsuario(fkUsuario);
    }

    public Categoria listarCategoria(Long idCategoria, Long fkUsuario){
        return categoriaExisteValidacao.obterCategoriaDoUsuario(idCategoria, fkUsuario);
    }

    @Transactional
    public Categoria atualizarCategoria(CategoriaRequestDTO dto,Long idCategoria, Long fkUsuario){
        Categoria c = listarCategoria(idCategoria, fkUsuario);
        c.atualizar(dto.nome(), dto.descricao());
        return categoriaRepository.save(c);
    }

    @Transactional
    public void removerCategoria(Long id){
        categoriaRepository.delete(categoriaExisteValidacao.obterCategoria(id));
    }
}
