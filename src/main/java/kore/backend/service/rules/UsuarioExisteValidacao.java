package kore.backend.service.rules;

import kore.backend.exception.UsuarioNaoExistente;
import kore.backend.model.Usuario;
import kore.backend.repository.UsuarioRepository;

public class UsuarioExisteValidacao implements UsuarioValidacao{
    public UsuarioRepository usuarioRepository;

    public UsuarioExisteValidacao(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean validar(Long idUsuario) {
        if(!usuarioRepository.existsById(idUsuario))
            throw new UsuarioNaoExistente(idUsuario);
        return true;
    }
}
