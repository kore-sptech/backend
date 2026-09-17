package kore.backend.service.validation;

import kore.backend.model.Transacao;

public interface TransacaoValidationService {
    void validarValor(Transacao transacao);
}
