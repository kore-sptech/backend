package kore.backend.service.policy;

import kore.backend.dto.produto.ProdutoDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Produto;
import kore.backend.repository.ProdutoRepository;
import kore.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class ProdutoPolicy {
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProdutoPolicy(ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void validarUsuario(Long fkUsuario) {
        if (!usuarioRepository.existsById(fkUsuario)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado", fkUsuario);
        }
    }

    public void validarCadastro(ProdutoDTO produtoDTO, Long fkUsuario) {
        validarUsuario(fkUsuario);

        if (produtoDTO == null) {
            throw new IllegalArgumentException("Produto inválido");
        }

        if (produtoDTO.nome() == null || produtoDTO.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }
    }

    public Produto validarProdutoDoUsuario(Long idProduto, Long fkUsuario) {
        validarUsuario(fkUsuario);
        return produtoRepository.findByIdAndFkUsuario(idProduto, fkUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado para este usuário", idProduto));
    }
}
