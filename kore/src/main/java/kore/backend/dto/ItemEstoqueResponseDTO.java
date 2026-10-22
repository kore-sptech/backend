package kore.backend.dto;

import kore.backend.model.Item;

import java.time.LocalDateTime;

public record ItemEstoqueResponseDTO(
        Long id,
        Float valorUnitario,
        LocalDateTime dataValidade,
        Boolean seAtivo,
        Long produtoId,
        String nomeProduto
) {
    public static ItemEstoqueResponseDTO fromEntity(Item item) {
        Long produtoId = null;
        String nomeProduto = null;

        if (item.getProduto() != null) {
            produtoId = item.getProduto().getId();
            nomeProduto = item.getProduto().getNome();
        }

        return new ItemEstoqueResponseDTO(
                item.getId(),
                item.getValorUnitario(),
                item.getDataValidade(),
                item.getSeAtivo(),
                produtoId,
                nomeProduto
        );
    }
}

