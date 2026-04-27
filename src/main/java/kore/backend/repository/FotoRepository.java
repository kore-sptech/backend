package kore.backend.repository;

import kore.backend.model.Agendamento;
import kore.backend.model.Foto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FotoRepository extends JpaRepository<Foto, Long> {
    long deleteAllByAgendamento(Agendamento agendamento);

    List<Foto> findAllByAgendamento(Agendamento agendamento);
}
