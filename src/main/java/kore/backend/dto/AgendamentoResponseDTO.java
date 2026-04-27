package kore.backend.dto;

import kore.backend.model.Agendamento;
import kore.backend.model.Foto;
import kore.backend.model.enums.FormaPagamento;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AgendamentoResponseDTO {
    private Long id;

    private Double preco;

    private String cliente;

    private String telefone;

    private FormaPagamento formaPagamento;

    private LocalDateTime inicio;

    private LocalDateTime fim;

    private List<Foto> referencias;

    public AgendamentoResponseDTO(Agendamento agendamento) {
        this.id = agendamento.getId();
        this.preco = agendamento.getPreco();
        this.cliente = agendamento.getCliente();
        this.telefone = agendamento.getTelefone();
        this.formaPagamento = agendamento.getFormaPagamento();
        this.inicio = agendamento.getInicio();
        this.fim = agendamento.getFim();
        this.referencias = agendamento.getReferencias().stream().map(
                f -> {
                    f.setImageUrl(f.getImageUrl().substring(1, f.getImageUrl().length()));
                    return f;
                }).toList();
    }
}
