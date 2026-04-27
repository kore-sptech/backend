package kore.backend.model;

import jakarta.persistence.*;
import kore.backend.model.enums.FormaPagamento;
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

    private LocalDateTime inicio;

    private LocalDateTime fim;

}
