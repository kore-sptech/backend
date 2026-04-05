package kore.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import kore.backend.dto.ItemDTO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table (name = "Item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dt_Entrada")
    @CreationTimestamp
    private LocalDateTime dataEntrada;

    @Column(name = "dt_validade")
    private LocalDateTime dataValidade;

    @Column(name = "vl_unitario")
    private Float valorUnitario;

    @Column(name = "dt_saida")
    private LocalDateTime dataSaida;

    @Column(name = "se_ativo")
    private Boolean seAtivo;

    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(name = "fk_produto", nullable = false)
    private Produto produto;

    public Item() {
    }

    public Item(ItemDTO itemDTO) {
        this.dataEntrada = itemDTO.dataEntrada();
        this.dataValidade = itemDTO.dataValidade();
        this.seAtivo = itemDTO.seAtivo();
        this.valorUnitario = itemDTO.valorUnitario();
    }
}
