package kore.backend.service.validation;

import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Produto;
import kore.backend.model.Usuario;
import kore.backend.repository.ProdutoRepository;
import kore.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultProdutoValidationService implements ProdutoValidationService {
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public DefaultProdutoValidationService(ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void validarUsuario(Long fkUsuario) {
        if (!usuarioRepository.existsById(fkUsuario)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado", fkUsuario);
        }
    }

    @Override
    public Produto validarProdutoDoUsuario(Long idProduto, Long fkUsuario) {
        return produtoRepository.findByIdAndFkUsuario(idProduto, fkUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado para este usuário", idProduto));
    }
}
