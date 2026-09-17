package kore.backend.service.validation;

import kore.backend.model.Agendamento;
import kore.backend.model.Usuario;
import kore.backend.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DefaultAgendamentoValidationService implements AgendamentoValidationService {
    private final AgendamentoRepository agendamentoRepository;

    public DefaultAgendamentoValidationService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    public void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Início e fim do agendamento são obrigatórios");
        }

        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("Fim do agendamento deve ser após o início");
        }
    }

    @Override
    public void validarConflitoHorario(LocalDateTime inicio, LocalDateTime fim, Usuario usuario, Long idIgnorado) {
        boolean existeConflitoNaAgenda = agendamentoRepository
                .existsByInicioLessThanAndFimGreaterThanAndUsuario(fim, inicio, usuario);

        if (!existeConflitoNaAgenda) {
            return;
        }

        if (idIgnorado == null) {
            throw new IllegalArgumentException("Já existe um agendamento nesse horário");
        }

        List<Agendamento> agendamentosEmConflito = agendamentoRepository.findByInicioBetweenAndUsuario(inicio, fim, usuario);
        boolean conflitoComOutro = agendamentosEmConflito.stream()
                .anyMatch(agendamento -> !agendamento.getId().equals(idIgnorado));

        if (conflitoComOutro) {
            throw new IllegalArgumentException("Já existe um agendamento nesse horário");
        }
    }
}
