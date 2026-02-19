package kore.backend.dto;

public record UsuarioDTO(
        String email,
        String nome,
        String senha
) {
}
