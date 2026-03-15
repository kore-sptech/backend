package kore.backend.service;

import jakarta.transaction.Transactional;
import kore.backend.dto.ProdutoDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Produto;
import kore.backend.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public Produto salvarProduto(ProdutoDTO produtoDTO) {
        Produto p = new Produto(produtoDTO);
        return produtoRepository.save(p);
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
