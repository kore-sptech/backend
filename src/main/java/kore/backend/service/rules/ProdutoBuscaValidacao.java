package kore.backend.service.rules;

import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Produto;
import kore.backend.repository.ProdutoRepository;
import org.springframework.stereotype.Component;

@Component
public class ProdutoBuscaValidacao implements ProdutoValidacao{
    private final ProdutoRepository produtoRepository;

    public ProdutoBuscaValidacao(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public Produto validarEBuscar(Long idProduto, Long fkUsuario) {
        return produtoRepository.findByIdAndFkUsuario(idProduto, fkUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado para este usuário", idProduto));
    }
}
