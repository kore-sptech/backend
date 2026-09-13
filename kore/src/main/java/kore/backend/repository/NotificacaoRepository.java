package kore.backend.repository;

import kore.backend.model.Agendamento;
import kore.backend.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {


    boolean existsByAgendamento(Agendamento agendamento);
}