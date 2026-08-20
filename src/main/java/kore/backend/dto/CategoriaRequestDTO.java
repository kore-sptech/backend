package kore.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
        @NotNull @Size(min=3, max=45) String nome,
        @Size (min=0, max=150) String descricao
) {
}
