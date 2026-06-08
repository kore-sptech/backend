package kore.backend.events;

import jakarta.transaction.Transactional;
import kore.backend.model.Agendamento;
import kore.backend.model.Notificacao;
import kore.backend.model.enums.StatusAgendamento;
import kore.backend.model.enums.TipoNotificacao;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.repository.NotificacaoRepository;
import kore.backend.service.ServerSentEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoObserver {

    private final NotificacaoRepository notificacaoRepository;
    private final ServerSentEventService serverSentEventService;
    private final AgendamentoRepository agendamentoRepository;

    @EventListener
    public void onAgendamentoProximo(AgendamentoProximoEvent event) {

        // 1. Persiste a notificação dentro de uma transação isolada
        NotificacaoComAgendamento resultado = salvarNotificacao(event.getAgendamentoId());

        // Retorno nulo = notificação já existia; nada a fazer
        if (resultado == null) return;

        // 2. Envia via SSE FORA da transação
        //    Erros de rede (Broken pipe) não afetam mais o commit do banco
        serverSentEventService.sendNotification(
                resultado.notificacao(),
                resultado.agendamento()
        );
    }

    // ── Transação isolada ─────────────────────────────────────────────────────

    /**
     * Toda a lógica de banco em uma única transação.
     * O envio SSE fica propositalmente fora daqui.
     *
     * @return NotificacaoComAgendamento ou null se já notificado
     */
    @Transactional
    protected NotificacaoComAgendamento salvarNotificacao(Long agendamentoId) {

        Agendamento agendamento = agendamentoRepository
                .findByIdWithReferencias(agendamentoId)
                .orElseThrow(() -> new RuntimeException(
                        "Agendamento não encontrado: " + agendamentoId));

        if (notificacaoRepository.existsByAgendamento(agendamento)) {
            log.info("Notificação já existente para agendamento ID: {}. Ignorando.", agendamentoId);
            return null;
        }

        long minutosRestantes = LocalDateTime.now()
                .until(agendamento.getInicio(), ChronoUnit.MINUTES);

        String mensagem = String.format(
                "Agendamento de %s começa em %d minutos.",
                agendamento.getCliente(),
                minutosRestantes
        );

        Notificacao notificacao = Notificacao.builder()
                .titulo("Agendamento Próximo")
                .mensagem(mensagem)
                .agendamento(agendamento)
                .tipo(TipoNotificacao.NORMAL)
                .build();

        notificacaoRepository.save(notificacao);

        agendamento.setStatus(StatusAgendamento.AGUARDANDO);
        agendamentoRepository.save(agendamento);

        log.info("Notificação salva para agendamento ID: {}", agendamento.getId());

        return new NotificacaoComAgendamento(notificacao, agendamento);
    }

    // ── Record auxiliar ───────────────────────────────────────────────────────

    private record NotificacaoComAgendamento(Notificacao notificacao, Agendamento agendamento) {
    }
}