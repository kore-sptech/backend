package kore.backend.model;

import jakarta.persistence.*;
import kore.backend.dto.ProdutoDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nome")
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "possuiValidade", nullable = false)
    private Boolean possuiValidade;

    @Column(name = "qtdMinAlerta")
    private Integer qtdMinAlerta;

    public Produto(ProdutoDTO produtoDTO) {
        this.qtdMinAlerta = produtoDTO.qtdMinAlerta();
        this.nome = produtoDTO.nome();
        this.descricao = produtoDTO.descricao();
        this.possuiValidade = produtoDTO.possuiValidade();
    }

    public Produto() {

    }
}
