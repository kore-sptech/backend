package kore.backend.service.validation;

import kore.backend.model.Usuario;

public interface UsuarioValidationService {
    boolean validar(Long idUsuario);
    Usuario buscarOuLancar(Long idUsuario);
    void validarEmailDisponivel(String email, Long idIgnorado);
}
