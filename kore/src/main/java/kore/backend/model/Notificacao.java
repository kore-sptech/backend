package kore.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import kore.backend.model.enums.TipoNotificacao;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String mensagem;

    private boolean lida = false;

    private String severidade; // CRITICO, ATENCAO, INFORMATIVO
    private String entidadeTipo; // APPOINTMENT, STOCK, etc.
    private Long entidadeId;
    private String acaoTipo; // REGISTRAR_PRODUTO, VER_AGENDAMENTO, etc.

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_fk")
    @JsonIgnore
    private Agendamento agendamento;

    @Enumerated(EnumType.STRING)
    private TipoNotificacao tipo;

    @CreationTimestamp
    private LocalDateTime criadaEm;

}
