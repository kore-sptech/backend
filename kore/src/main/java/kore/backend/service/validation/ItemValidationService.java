package kore.backend.service.validation;

import kore.backend.model.Agendamento;
import kore.backend.model.Produto;

public interface ItemValidationService {
    Produto buscarProdutoOuLancar(Long idProduto);
    Agendamento buscarAgendamentoOuLancar(Long idAgendamento);
}
