package kore.backend.service.validation;

import kore.backend.model.Transacao;
import org.springframework.stereotype.Service;

@Service
public class DefaultTransacaoValidationService implements TransacaoValidationService {
    @Override
    public void validarValor(Transacao transacao) {
        if (transacao.getValor() == null || transacao.getValor() <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser maior que zero");
        }
    }
}
