package kore.backend.service;

import jakarta.transaction.Transactional;
import kore.backend.dto.CategoriaRequestDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Categoria;
import kore.backend.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }
    @Transactional
    public Categoria adicionarCategoria(CategoriaRequestDTO dto, Long fkUsuario){
        Categoria categoria = new Categoria();
        categoria.setNome(dto.nome());
        categoria.setDescricao(dto.descricao());
        categoria.setFkUsuario(fkUsuario);
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarCategorias(Long fkUsuario){
        return categoriaRepository.buscarPorIdDoUsuario(fkUsuario)
                .orElse(Collections.emptyList());
    }

    public Categoria listarCategoria(Long idCategoria, Long fkUsuario){
        return (Categoria) categoriaRepository.buscarCategoriaPorIdDaCategoriaEDoUsuario(fkUsuario, idCategoria)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada", idCategoria));
    }

    @Transactional
    public Categoria atualizarCategoria(CategoriaRequestDTO dto,Long idCategoria, Long fkUsuario){
        Categoria c = listarCategoria(idCategoria, fkUsuario);
        c.setNome(dto.nome());
        c.setDescricao(dto.descricao());
        return categoriaRepository.save(c);
    }

    @Transactional
    public void removerCategoria(Long id){
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada", id));
        categoriaRepository.delete(c);
    }
}
