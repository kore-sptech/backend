package kore.backend.mapper;

import kore.backend.dto.AgendamentoRequestDTO;
import kore.backend.model.Agendamento;
import kore.backend.model.Usuario;
import kore.backend.model.enums.StatusAgendamento;

public final class AgendamentoMapper {
    private AgendamentoMapper() {
    }

    public static Agendamento fromRequest(AgendamentoRequestDTO request, Usuario usuario) {
        return Agendamento.builder()
                .cliente(request.getCliente())
                .telefone(request.getTelefone())
                .formaPagamento(request.getFormaPagamento())
                .preco(request.getPreco())
                .inicio(request.getInicio())
                .fim(request.getFim())
                .usuario(usuario)
                .status(StatusAgendamento.PENDENTE)
                .build();
    }

    public static void applyChanges(Agendamento target, AgendamentoRequestDTO request) {
        target.setPreco(request.getPreco());
        target.setCliente(request.getCliente());
        target.setTelefone(request.getTelefone());
        target.setFormaPagamento(request.getFormaPagamento());
        target.setInicio(request.getInicio());
        target.setFim(request.getFim());
    }
}
