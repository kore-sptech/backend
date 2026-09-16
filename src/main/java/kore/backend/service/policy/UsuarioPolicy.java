package kore.backend.service.policy;

import kore.backend.dto.UsuarioDTO;
import kore.backend.exception.CredencialExistenteException;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Usuario;
import kore.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioPolicy {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioPolicy(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario validarLogin(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado", 0L));

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new RecursoNaoEncontradoException("Senha incorreta", usuario.getId());
        }

        return usuario;
    }

    public void validarCadastro(UsuarioDTO usuarioDTO) {
        usuarioRepository.findByEmail(usuarioDTO.email())
                .ifPresent(usuario -> {
                    throw new CredencialExistenteException("E-mail já cadastrado.", usuarioDTO.email());
                });
    }

    public void validarAtualizacao(UsuarioDTO usuarioDTO, Long id) {
        usuarioRepository.findByEmail(usuarioDTO.email())
                .ifPresent(usuarioExistente -> {
                    if (!usuarioExistente.getId().equals(id)) {
                        throw new CredencialExistenteException("E-mail já cadastrado.", usuarioDTO.email());
                    }
                });
    }

    public void validarExistencia(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado", id);
        }
    }
}
