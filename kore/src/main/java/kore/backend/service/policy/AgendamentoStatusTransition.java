package kore.backend.service.policy;

import kore.backend.model.Agendamento;
import kore.backend.model.Usuario;
import kore.backend.model.enums.StatusAgendamento;

public enum AgendamentoStatusTransition {
    PENDENTE {
        @Override
        public void validateConfirmacao(Agendamento agendamento, Usuario usuario) {
            assertOwner(agendamento, usuario);
        }

        @Override
        public void validatePagamento(Agendamento agendamento) {
            // sem restrição extra além de status válido
        }

        @Override
        public void validateCancelamento(Agendamento agendamento) {
            // sem restrição extra além de status válido
        }
    },
    AGUARDANDO {
        @Override
        public void validateConfirmacao(Agendamento agendamento, Usuario usuario) {
            assertOwner(agendamento, usuario);
        }

        @Override
        public void validatePagamento(Agendamento agendamento) {
            // sem restrição extra além de status válido
        }

        @Override
        public void validateCancelamento(Agendamento agendamento) {
            // sem restrição extra além de status válido
        }
    },
    CONFIRMADO {
        @Override
        public void validateConfirmacao(Agendamento agendamento, Usuario usuario) {
            throw new IllegalArgumentException("Este agendamento já está confirmado");
        }

        @Override
        public void validatePagamento(Agendamento agendamento) {
            // pagamento já confirmado está em outra transição
        }

        @Override
        public void validateCancelamento(Agendamento agendamento) {
            // cancelamento pode ser permitido em outra etapa
        }
    },
    CONFIRMADO_PAGAMENTO {
        @Override
        public void validateConfirmacao(Agendamento agendamento, Usuario usuario) {
            throw new IllegalArgumentException("Este agendamento já está confirmado");
        }

        @Override
        public void validatePagamento(Agendamento agendamento) {
            throw new IllegalArgumentException("Este agendamento já está confirmado");
        }

        @Override
        public void validateCancelamento(Agendamento agendamento) {
            // regra específica de cancelamento depende do fluxo
        }
    },
    CANCELADO {
        @Override
        public void validateConfirmacao(Agendamento agendamento, Usuario usuario) {
            throw new IllegalArgumentException("Não é possível confirmar o pagamento de um agendamento cancelado");
        }

        @Override
        public void validatePagamento(Agendamento agendamento) {
            throw new IllegalArgumentException("Não é possível confirmar o pagamento de um agendamento cancelado");
        }

        @Override
        public void validateCancelamento(Agendamento agendamento) {
            throw new IllegalArgumentException("Este agendamento já está cancelado");
        }
    };

    public abstract void validateConfirmacao(Agendamento agendamento, Usuario usuario);
    public abstract void validatePagamento(Agendamento agendamento);
    public abstract void validateCancelamento(Agendamento agendamento);

    public static AgendamentoStatusTransition from(StatusAgendamento status) {
        if (status == null) {
            throw new IllegalArgumentException("Status do agendamento não informado");
        }

        return switch (status) {
            case PENDENTE -> PENDENTE;
            case AGUARDANDO -> AGUARDANDO;
            case CONFIRMADO -> CONFIRMADO;
            case CONFIRMADO_PAGAMENTO -> CONFIRMADO_PAGAMENTO;
            case CANCELADO -> CANCELADO;
        };
    }

    private static void assertOwner(Agendamento agendamento, Usuario usuario) {
        if (!agendamento.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Usuário não autorizado para confirmar este agendamento");
        }
    }
}
