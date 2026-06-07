package kore.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kore.backend.model.Agendamento;
import kore.backend.model.Usuario;

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

        List<Agendamento> findByInicioBetweenAndUsuario(
                        LocalDateTime inicio,
                        LocalDateTime fim,
                        Usuario usuario);
}
