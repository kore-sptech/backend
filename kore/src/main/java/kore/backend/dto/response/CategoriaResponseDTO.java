package kore.backend.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaResponseDTO(
        @NotNull Long id,
        @NotNull @Size(min=3, max=45) String nome,
        @Size(min=0, max=150) String descricao
) {
}
