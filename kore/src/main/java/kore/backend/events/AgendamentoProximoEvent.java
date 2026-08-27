package kore.backend.events;

import org.springframework.context.ApplicationEvent;

/**
 * Evento contendo apenas o ID do agendamento. Transmitir entidades JPA entre
 * threads/sessions é perigoso (coleções lazy não ficam inicializadas) —
 * preferimos enviar o id e re-obter a entidade dentro de uma transação
 * no listener.
 */
public class AgendamentoProximoEvent extends ApplicationEvent {

    private final Long agendamentoId;

    public AgendamentoProximoEvent(Object source, Long agendamentoId) {
        super(source);
        this.agendamentoId = agendamentoId;
    }

    public Long getAgendamentoId() {
        return agendamentoId;
    }
}