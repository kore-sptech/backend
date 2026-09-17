package kore.backend.service.validation;

import kore.backend.model.Usuario;

import java.time.LocalDateTime;

public interface AgendamentoValidationService {
    void validarPeriodo(LocalDateTime inicio, LocalDateTime fim);
    void validarConflitoHorario(LocalDateTime inicio, LocalDateTime fim, Usuario usuario, Long idIgnorado);
}
