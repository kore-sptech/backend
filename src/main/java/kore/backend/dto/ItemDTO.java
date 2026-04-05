package kore.backend.dto;

import java.time.LocalDateTime;

public record ItemDTO(
        LocalDateTime dataEntrada,
        Boolean seAtivo,
        Float valorUnitario,
        LocalDateTime dataValidade
) {
}
