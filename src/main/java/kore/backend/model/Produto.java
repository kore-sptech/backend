package kore.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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

    @Column(name = "fkUsuario")
    private Long fkUsuario;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "imagem_key")
    private String imagemKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("produtos")
    @JoinColumn(name = "fk_categoria")
    private Categoria categoria;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> itens = new ArrayList<>();

    public Produto() {
    }

    public Produto(Long id, String nome, String descricao, Boolean possuiValidade, Integer qtdMinAlerta, Long fkUsuario, String tipo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.possuiValidade = possuiValidade;
        this.qtdMinAlerta = qtdMinAlerta;
        this.fkUsuario = fkUsuario;
        this.tipo = tipo;
    }

    public void atualizarProduto(
            String descricao,
            String nome,
            Integer qtdMinAlerta,
            String tipo
    ){
        this.descricao = descricao;
        this.nome = nome;
        this.qtdMinAlerta = qtdMinAlerta;
        this.tipo = tipo;
    }
    public void adicionarEstoque(List<Item> itens){
        itens.forEach(item -> itens.add(item));
    }
}
