package kore.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kore.backend.model.enums.FormaPagamento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AgendamentoRequestDTO {

    @Positive(message = "O preço deve ser um valor positivo")
    @NotNull(message = "O preço é obrigatório")
    private Double preco;

    @NotBlank(message = "O nome do cliente é obrigatório")
    private String cliente;

    private String telefone;

    private FormaPagamento formaPagamento;

    @Future(message = "A data de início deve ser uma data futura")
    private LocalDateTime inicio;

    @Future(message = "A data de fim deve ser uma data futura")
    private LocalDateTime fim;

    @NotEmpty(message = "As referências do agendamento são obrigatórias")
    private List<Long> referencias;

    // @NotEmpty(message = "Os itens do agendamento são obrigatórios")
    // private List<Long> items;

}
