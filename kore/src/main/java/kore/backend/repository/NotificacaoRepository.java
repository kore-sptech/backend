package kore.backend.repository;

import kore.backend.model.Agendamento;
import kore.backend.model.Notificacao;
import kore.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    boolean existsByAgendamento(Agendamento agendamento);

    List<Notificacao> findByUsuario(Usuario usuario);

    List<Notificacao> findByUsuarioAndLidaFalse(Usuario usuario);

    Optional<Notificacao> findByIdAndUsuario(Long id, Usuario usuario);
}