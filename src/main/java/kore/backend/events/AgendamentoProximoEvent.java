package kore.backend.events;

import kore.backend.model.Agendamento;
import org.springframework.context.ApplicationEvent;

public class AgendamentoProximoEvent extends ApplicationEvent {

    private final Agendamento agendamento;

    public AgendamentoProximoEvent(Object source, Agendamento agendamento) {
        super(source);
        this.agendamento = agendamento;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }
}