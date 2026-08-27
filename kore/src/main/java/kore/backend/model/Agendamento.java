package kore.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import kore.backend.dto.AgendamentoRequestDTO;
import kore.backend.model.enums.FormaPagamento;
import kore.backend.model.enums.StatusAgendamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "Agendamento")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double preco;

    private String cliente;

    private String telefone;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    @OneToMany(mappedBy = "agendamento")
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "agendamento")
    private List<Foto> referencias = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "fk_usuario")
    private Usuario usuario;

    private LocalDateTime inicio;

    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusAgendamento status = StatusAgendamento.PENDENTE;

    @OneToOne(mappedBy = "sessao", cascade = CascadeType.ALL)
    @JsonIgnore
    private Transacao transacao;

    public void put(AgendamentoRequestDTO agendamento) {
        this.preco = agendamento.getPreco();
        this.cliente = agendamento.getCliente();
        this.telefone = agendamento.getTelefone();
        this.formaPagamento = agendamento.getFormaPagamento();
        this.inicio = agendamento.getInicio();
        this.fim = agendamento.getFim();
    }

}
