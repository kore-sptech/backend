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
    @Transactional
    public void onAgendamentoProximo(AgendamentoProximoEvent event) {
        Long agendamentoId = event.getAgendamentoId();

        // Recarrega a entidade com referências inicializadas dentro da transação
        Agendamento agendamento = agendamentoRepository.findByIdWithReferencias(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado: " + agendamentoId));

        if (notificacaoRepository.existsByAgendamento(agendamento)) {
            log.info("Notificação já existente para agendamento ID: {}. Ignorando.", agendamento.getId());
            return;
        }

        long diferencaEmMinutos = LocalDateTime.now().until(agendamento.getInicio(), ChronoUnit.MINUTES);

        String mensagem = String.format(
                "Agendamento de %s começa em %d minutos.",
                agendamento.getCliente(),
                diferencaEmMinutos
        );

        Notificacao notificacao = Notificacao.builder()
                .titulo("Agendamento Próximo")
                .mensagem(mensagem)
                .agendamento(agendamento)
                .tipo(TipoNotificacao.NORMAL)
                .build();

        notificacaoRepository.save(notificacao);

        serverSentEventService.sendNotification(notificacao, agendamento);

        agendamento.setStatus(StatusAgendamento.AGUARDANDO);
        agendamentoRepository.save(agendamento);

        log.info("Notificação salva para agendamento ID: {}", agendamento.getId());
    }
}