package kore.backend.model;

import jakarta.persistence.*;
import kore.backend.dto.ProdutoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProduto")
    private Long id;
    @Column(name = "nome")
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "possuiValidade", nullable = false)
    private Boolean possuiValidade;

    @Column(name = "qtdMinAlerta")
    private Integer qtdMinAlerta;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> itens = new ArrayList<>();
    public Produto(ProdutoDTO produtoDTO) {
        this.qtdMinAlerta = produtoDTO.qtdMinAlerta();
        this.nome = produtoDTO.nome();
        this.descricao = produtoDTO.descricao();
        this.possuiValidade = produtoDTO.possuiValidade();
    }

    public Produto() {
    }

    public void adicionarEstoque(List<Item> itens){
        itens.forEach(item -> itens.add(item));
    }
}
