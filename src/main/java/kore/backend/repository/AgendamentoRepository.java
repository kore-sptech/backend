package kore.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kore.backend.model.Agendamento;
import kore.backend.model.Usuario;
import kore.backend.model.enums.StatusAgendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

        List<Agendamento> findByInicioGreaterThanEqualAndInicioLessThanOrderByInicioDesc(
                        LocalDateTime inicioInclusivo,
                        LocalDateTime fimExclusivo);

        boolean existsByInicioLessThanAndFimGreaterThanAndUsuario(
                        LocalDateTime fimExclusivo,
                        LocalDateTime inicioExclusivo,
                        Usuario usuario);

        boolean existsByInicioLessThanAndFimGreaterThan(
                        LocalDateTime fimExclusivo,
                        LocalDateTime inicioExclusivo);

        List<Agendamento> findByInicioBetweenAndStatusAgendamento(
                        LocalDateTime inicio,
                        LocalDateTime fim,
                        StatusAgendamento statusAgendamento);

        List<Agendamento> findByInicioBetweenAndUsuario(
                        LocalDateTime inicio,
                        LocalDateTime fim,
                        Usuario usuario);

        List<Agendamento> findByInicioBetween(
                        LocalDateTime inicio,
                        LocalDateTime fim);

}
