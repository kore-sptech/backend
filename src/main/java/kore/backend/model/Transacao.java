package kore.backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kore.backend.dto.TransacaoDTO;
import kore.backend.model.enums.CategoriaTransacao;
import kore.backend.model.enums.TipoTransacao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transacoes")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Double valor;

    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipo;

    @Enumerated(EnumType.STRING)
    private CategoriaTransacao categoria;

    @CreationTimestamp
    private LocalDateTime dataCriacao;

    public Transacao(TransacaoDTO transacaoDTO) {
        this.valor = transacaoDTO.valor();
        this.nome = transacaoDTO.nome();
        this.tipo = transacaoDTO.tipo();

        if (transacaoDTO.tipo().equals(TipoTransacao.ENTRADA)) {
            this.categoria = null;
        } else {
            this.categoria = transacaoDTO.categoria();
        }
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
