package kore.backend.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoDTO(
        @NotNull
        @Size(min = 3, max = 45)
        String nome,
        @NotNull
        @Size(min = 0, max = 80)
        String descricao,
        @NotNull
        Boolean possuiValidade,
        @NotNull
        @Min(0)
        Integer qtdMinAlerta,
        @NotNull
        String tipo
) {}