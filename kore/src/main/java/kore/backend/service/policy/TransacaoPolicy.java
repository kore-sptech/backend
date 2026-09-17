package kore.backend.service.policy;

import kore.backend.model.Transacao;
import org.springframework.stereotype.Component;

@Component
public class TransacaoPolicy {
    public void validarValor(Transacao transacao) {
        if (transacao.getValor() == null || transacao.getValor() <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser maior que zero");
        }
    }
}
