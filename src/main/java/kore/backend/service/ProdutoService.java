package kore.backend.service;

import jakarta.transaction.Transactional;
import kore.backend.dto.produto.ProdutoDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Produto;
import kore.backend.repository.ProdutoRepository;
import kore.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProdutoService(ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Produto salvarProduto(ProdutoDTO produtoDTO) {
        if(usuarioRepository.existsById(produtoDTO.usuario())){
            Produto p = new Produto(produtoDTO);
            return produtoRepository.save(p);
        }
        throw new RecursoNaoEncontradoException("Usuario nao encontrado", produtoDTO.usuario());
    }

    public List<Produto> listarTodosProdutos() {
        return produtoRepository.findAll();
    }

    @Transactional
    public Produto atualizarProduto(Long id, ProdutoDTO produtoDTO) {
        Produto p = produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado", id));
        p.setDescricao(produtoDTO.descricao());
        p.setNome(produtoDTO.nome());
        p.setQtdMinAlerta(produtoDTO.qtdMinAlerta());
        p.setTipo(produtoDTO.tipo());
        return produtoRepository.save(p);
    }

    @Transactional
    public void deletarProduto(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado", id);
        }
        produtoRepository.deleteById(id);
    }
}
