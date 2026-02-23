package kore.backend.service;

import exception.CredencialExistenteException;
import exception.RecursoNaoEncontradoException;
import org.springframework.transaction.annotation.Transactional;
import kore.backend.dto.UsuarioDTO;
import kore.backend.model.Usuario;
import kore.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario salvar (UsuarioDTO usuarioDTO){
        if (usuarioRepository.buscarEmail(usuarioDTO.email()) != null){
            throw new CredencialExistenteException("E-mail já cadastrado.",usuarioDTO.email());
        };
        Usuario p = new Usuario();
        p.setEmail(usuarioDTO.email());
        p.setNome(usuarioDTO.nome());
        p.setSenha(usuarioDTO.senha());

        return usuarioRepository.save(p);
    }
    public Usuario buscar(Long id){
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado", id));
    }


    @Transactional
    public Usuario atualizar(UsuarioDTO usuarioDTO, Long id){
        Usuario p = usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado", id));
        p.setEmail(usuarioDTO.email());
        p.setSenha(usuarioDTO.senha());
        p.setNome(usuarioDTO.nome());
        return usuarioRepository.save(p);
    }
    @Transactional
    public void deletar(Long id){
        if(! usuarioRepository.existsById(id)){
            throw new RecursoNaoEncontradoException("Usuário não encontrado", id);
        }
        usuarioRepository.deleteById(id);
    }
}
