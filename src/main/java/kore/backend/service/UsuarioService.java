package kore.backend.service;

import kore.backend.exception.CredencialExistenteException;
import kore.backend.exception.RecursoNaoEncontradoException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import kore.backend.dto.UsuarioDTO;
import kore.backend.model.Usuario;
import kore.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario salvar(UsuarioDTO usuarioDTO) {
        //troquei o buscarEmail por findByEmail
        if (usuarioRepository.findByEmail(usuarioDTO.email()).isPresent()) {
            throw new CredencialExistenteException("E-mail já cadastrado.", usuarioDTO.email());
        }

        Usuario p = new Usuario();
        p.setEmail(usuarioDTO.email());
        p.setNome(usuarioDTO.nome());
        p.setSenha(passwordEncoder.encode(usuarioDTO.senha()));

        return usuarioRepository.save(p);
    }

    public Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado", id));
    }

//    @Transactional
//    public Usuario atualizar(UsuarioDTO usuarioDTO, Long id) {
//        Usuario p = usuarioRepository.findById(id)
//                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado", id));
//
//        if (usuarioRepository.findByEmail(usuarioDTO.email()) != null
//                && !usuarioRepository.findByEmail(usuarioDTO.email()).getId().equals(id)) {
//            throw new CredencialExistenteException("E-mail já cadastrado.", usuarioDTO.email());
//        }
//
//        p.setEmail(usuarioDTO.email());
//        p.setSenha(usuarioDTO.senha());
//        p.setNome(usuarioDTO.nome());
//        return usuarioRepository.save(p);
//    }


    @Transactional
    public Usuario atualizar(UsuarioDTO usuarioDTO, Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado", id));

        usuarioRepository.findByEmail(usuarioDTO.email())
                .ifPresent(usuarioExistente -> {
                    if (!usuarioExistente.getId().equals(id)) {
                        throw new CredencialExistenteException("E-mail já cadastrado.", usuarioDTO.email());
                    }
                });

        usuario.setEmail(usuarioDTO.email());
        usuario.setSenha(usuarioDTO.senha());
        usuario.setNome(usuarioDTO.nome());

        return usuarioRepository.save(usuario);
    }


    @Transactional
    public void deletar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado", id);
        }
        usuarioRepository.deleteById(id);
    }

    public List<Usuario> buscartodos() {
        return this.usuarioRepository.findAll();
    }

    public Usuario login(String email, String senha) {
        Usuario usuario = this.usuarioRepository.findByEmail(email).orElseThrow(
                () -> new RecursoNaoEncontradoException("Usuário não encontrado", 1L));

        if (!usuario.getSenha().equals(senha)) {
            throw new RecursoNaoEncontradoException("Senha incorreta", 1L);
        }

        return usuario;
    }
}
