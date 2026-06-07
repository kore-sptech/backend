package kore.backend.events;

import kore.backend.model.enums.StatusAgendamento;
import kore.backend.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgendamentoScheduler {

    private final AgendamentoRepository agendamentoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 60_000) // a cada 1 minuto é suficiente para janela de ±1 min
    public void verificarAgendamentosProximos() {
        LocalDateTime agora = LocalDateTime.now();

        // Janela de ±1 minuto em torno dos 10 minutos — evita tanto perder quanto
        // duplicar
        LocalDateTime janelaInicio = agora.plusMinutes(9);
        LocalDateTime janelaFim = agora.plusMinutes(11);

        log.info("Scheduler rodando. Buscando agendamentos entre {} e {}", janelaInicio, janelaFim);
        agendamentoRepository
                .findByInicioBetweenAndStatusAgendamento(janelaInicio, janelaFim, StatusAgendamento.PENDENTE)
                .forEach(agendamento -> {
                    eventPublisher.publishEvent(new AgendamentoProximoEvent(this, agendamento));
                    log.info("Evento publicado para agendamento ID: {}", agendamento.getId());
                });
    }
}