package kore.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "Foto")
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String imageUrl;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "fk_agendamento")
    private Agendamento agendamento;


    public Foto(String imageUrl, String nome) {
        this.imageUrl = imageUrl;
        this.nome = nome;
    }
}
