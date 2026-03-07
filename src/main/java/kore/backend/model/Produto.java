package kore.backend.model;

import jakarta.persistence.*;


@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nome")
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "possuiValidade")
    private Boolean possuiValidade;

    @Column(name = "qtdMinAlerta")
    private Integer qtdMinAlerta;
}
