package kore.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemDTO(
        LocalDateTime dataEntrada,
        Boolean seAtivo,
        BigDecimal valorUnitario,
        LocalDateTime dataValidade
) {
}
