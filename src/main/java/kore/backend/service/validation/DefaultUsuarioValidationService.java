package kore.backend.service.validation;

import kore.backend.exception.CredencialExistenteException;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Usuario;
import kore.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultUsuarioValidationService implements UsuarioValidationService {
    private final UsuarioRepository usuarioRepository;

    public DefaultUsuarioValidationService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean validar(Long idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado", idUsuario);
        }
        return true;
    }

    @Override
    public Usuario buscarOuLancar(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado", idUsuario));
    }

    @Override
    public void validarEmailDisponivel(String email, Long idIgnorado) {
        usuarioRepository.findByEmail(email)
                .ifPresent(usuarioExistente -> {
                    if (idIgnorado == null || !usuarioExistente.getId().equals(idIgnorado)) {
                        throw new CredencialExistenteException("E-mail já cadastrado.", email);
                    }
                });
    }
}
