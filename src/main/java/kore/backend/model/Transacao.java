package kore.backend.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import kore.backend.dto.TransacaoDTO;
import kore.backend.model.enums.CategoriaTransacao;
import kore.backend.model.enums.TipoTransacao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Transacao")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "idTransacao")
    private Long id;

    private Double valor;

    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipo;

    @Enumerated(EnumType.STRING)
    private CategoriaTransacao categoria;

    @CreationTimestamp
    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    @JsonIgnore
    private Usuario usuario;


    @OneToOne
    @JoinColumn(name = "sessao_id")
    private Agendamento sessao;

    public Transacao(TransacaoDTO transacaoDTO) {
        this.valor = transacaoDTO.valor();
        this.nome = transacaoDTO.nome();
        this.tipo = transacaoDTO.tipo();
        this.categoria = transacaoDTO.categoria();
    }

    @Override
    public String toString() {
        return "Transacao{" +
                "id=" + id +
                ", valor=" + valor +
                ", nome='" + nome + '\'' +
                ", tipo=" + tipo +
                ", categoria=" + categoria +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
