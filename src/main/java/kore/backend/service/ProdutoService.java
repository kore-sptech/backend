package kore.backend.service;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import kore.backend.dto.produto.ProdutoDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Produto;
import kore.backend.repository.CategoriaRepository;
import kore.backend.repository.ProdutoRepository;
import kore.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public Produto salvarProduto(ProdutoDTO produtoDTO, Long fkUsuario) {
        // Ajustado para validar o fkUsuario da rota em vez do DTO
        if(usuarioRepository.existsById(fkUsuario)){
            Produto p = new Produto(produtoDTO);
            p.setCategoria(categoriaRepository.findById(produtoDTO.categoriaId())
                    .orElseThrow(() -> new EntityExistsException("Id da categoria não existe"))
            );
            p.setFkUsuario(fkUsuario); // Como já validamos que existe, podemos setar direto

            return produtoRepository.save(p);
        }
        throw new RecursoNaoEncontradoException("Usuario nao encontrado", fkUsuario);
    }

    public List<Produto> listarTodosProdutos(Long fkUsuario) {
        if(!usuarioRepository.existsById(fkUsuario)) {
            throw new RecursoNaoEncontradoException("Usuario nao encontrado", fkUsuario);
        }
        // Exige um novo método no ProdutoRepository
        return produtoRepository.findAllByFkUsuario(fkUsuario);
    }

    @Transactional
    public Produto atualizarProduto(Long fkUsuario, Long id, ProdutoDTO produtoDTO) {
        // Exige um novo método no ProdutoRepository
        Produto p = produtoRepository.findByIdAndFkUsuario(id, fkUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado para este usuário", id));

        p.setDescricao(produtoDTO.descricao());
        p.setNome(produtoDTO.nome());
        p.setQtdMinAlerta(produtoDTO.qtdMinAlerta());
        p.setTipo(produtoDTO.tipo());

        return produtoRepository.save(p);
    }

    @Transactional
    public void deletarProduto(Long fkUsuario, Long id) {
        // Exige um novo método no ProdutoRepository
        Produto p = produtoRepository.findByIdAndFkUsuario(id, fkUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado para este usuário", id));

        produtoRepository.delete(p);
    }
}