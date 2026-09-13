package kore.backend.dto;

import jakarta.validation.constraints.NotBlank;
import kore.backend.model.enums.CategoriaTransacao;
import kore.backend.model.enums.TipoTransacao;

public record TransacaoDTO(
        @NotBlank(message = "O valor da transação é obrigatório") Double valor,
        @NotBlank(message = "O nome da transação é obrigatório") String nome,
        @NotBlank(message = "O tipo da transação é obrigatório") TipoTransacao tipo,
        @NotBlank(message = "A categoria da transação é obrigatória") CategoriaTransacao categoria) {

    @Override
    public String toString() {
        return "TransacaoDTO{" +
                "valor=" + valor +
                ", nome='" + nome + '\'' +
                ", tipo=" + tipo +
                ", categoria=" + categoria +
                '}';
    }
}
