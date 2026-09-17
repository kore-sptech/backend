package kore.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;
    @Column(name = "nome")
    private String nome;
    @Column(name = "descricao")
    private String descricao;
    @Column(name = "fk_usuario")
    private Long fkUsuario;

    @OneToMany(mappedBy = "categoria")
    private List<Produto> produtos = new ArrayList<>();

    public Categoria() {
    }

    public Categoria(String nome, String descricao, Long fkUsuario) {
        this.nome = nome;
        this.descricao = descricao;
        this.fkUsuario = fkUsuario;
    }
    public void atualizar(String nome, String descricao){
        this.nome = nome;
        this.descricao = descricao;
    }
}
