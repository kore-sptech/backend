package kore.backend.dto.response;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email
) {
}
