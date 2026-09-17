package kore.backend.service.policy;

import kore.backend.dto.AgendamentoRequestDTO;
import kore.backend.model.Agendamento;
import kore.backend.model.Usuario;
import kore.backend.model.enums.StatusAgendamento;
import kore.backend.repository.AgendamentoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AgendamentoPolicy {
    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoPolicy(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public void validarCriacao(AgendamentoRequestDTO request, Usuario usuario) {
        validarPeriodo(request.getInicio(), request.getFim());
        validarConflito(request.getInicio(), request.getFim(), usuario, null);
    }

    public void validarAtualizacao(AgendamentoRequestDTO request, Usuario usuario, Long id) {
        validarPeriodo(request.getInicio(), request.getFim());
        validarConflito(request.getInicio(), request.getFim(), usuario, id);
    }

    public void validarConfirmacao(Agendamento agendamento, Usuario usuario) {
        AgendamentoStatusTransition.from(agendamento.getStatus())
                .validateConfirmacao(agendamento, usuario);
    }

    public void validarPagamento(Agendamento agendamento) {
        AgendamentoStatusTransition.from(agendamento.getStatus())
                .validatePagamento(agendamento);
    }

    public void validarCancelamento(Agendamento agendamento) {
        AgendamentoStatusTransition.from(agendamento.getStatus())
                .validateCancelamento(agendamento);
    }

    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Início e fim do agendamento são obrigatórios");
        }

        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("Fim do agendamento deve ser após o início");
        }
    }

    private void validarConflito(LocalDateTime inicio, LocalDateTime fim, Usuario usuario, Long idIgnorado) {
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
