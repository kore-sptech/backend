package kore.backend.service.validation;

import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Agendamento;
import kore.backend.model.Produto;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultItemValidationService implements ItemValidationService {
    private final ProdutoRepository produtoRepository;
    private final AgendamentoRepository agendamentoRepository;

    public DefaultItemValidationService(ProdutoRepository produtoRepository, AgendamentoRepository agendamentoRepository) {
        this.produtoRepository = produtoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    public Produto buscarProdutoOuLancar(Long idProduto) {
        return produtoRepository.findById(idProduto)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado", idProduto));
    }

    @Override
    public Agendamento buscarAgendamentoOuLancar(Long idAgendamento) {
        return agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado", idAgendamento));
    }
}
