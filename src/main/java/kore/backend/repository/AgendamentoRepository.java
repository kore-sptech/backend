package kore.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kore.backend.model.Agendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

        List<Agendamento> findByInicioGreaterThanEqualAndInicioLessThanOrderByInicioDesc(
                        LocalDateTime inicioInclusivo,
                        LocalDateTime fimExclusivo);

        boolean existsByInicioLessThanAndFimGreaterThan(
                        LocalDateTime fimExclusivo,
                        LocalDateTime inicioExclusivo);

        List<Agendamento> findByInicioBetween(
                        LocalDateTime inicio,
                        LocalDateTime fim);
}
