package kore.backend.service;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import kore.backend.dto.produto.ProdutoDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Produto;
import kore.backend.repository.CategoriaRepository;
import kore.backend.repository.ProdutoRepository;
import kore.backend.repository.UsuarioRepository;
import kore.backend.service.rules.CategoriaExisteValidacao;
import kore.backend.service.rules.ProdutoBuscaValidacao;
import kore.backend.service.rules.UsuarioExisteValidacao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final CategoriaExisteValidacao categoriaExisteValidacao;
    private final UsuarioExisteValidacao usuarioExisteValidacao;
    private final ProdutoBuscaValidacao produtoBuscaValidacao;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaExisteValidacao categoriaExisteValidacao, UsuarioExisteValidacao usuarioExisteValidacao, ProdutoBuscaValidacao produtoBuscaValidacao) {
        this.produtoRepository = produtoRepository;
        this.categoriaExisteValidacao = categoriaExisteValidacao;
        this.usuarioExisteValidacao = usuarioExisteValidacao;
        this.produtoBuscaValidacao = produtoBuscaValidacao;
    }

    @Transactional
    public Produto salvarProduto(ProdutoDTO produtoDTO, Long fkUsuario) {
        usuarioExisteValidacao.validar(fkUsuario);
        Produto p = new Produto(produtoDTO);
        p.setCategoria(categoriaExisteValidacao.obterCategoria(produtoDTO.categoriaId()));
        p.setFkUsuario(fkUsuario);
        return produtoRepository.save(p);
    }

    public List<Produto> listarTodosProdutos(Long fkUsuario) {
        usuarioExisteValidacao.validar(fkUsuario);
        // Exige um novo método no ProdutoRepository
        return produtoRepository.findAllByFkUsuario(fkUsuario);
    }

    @Transactional
    public Produto atualizarProduto(Long fkUsuario, Long id, ProdutoDTO produtoDTO) {
        Produto p = produtoBuscaValidacao.validarEBuscar(id, fkUsuario);
        p.atualizarProduto(
                produtoDTO.descricao(),
                produtoDTO.nome(),
                produtoDTO.qtdMinAlerta(),
                produtoDTO.tipo()
        );
        return produtoRepository.save(p);
    }

    @Transactional
    public void deletarProduto(Long fkUsuario, Long id) {
        Produto p = produtoBuscaValidacao.validarEBuscar(id, fkUsuario);
        produtoRepository.delete(p);
    }
}