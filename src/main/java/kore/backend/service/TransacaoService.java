package kore.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kore.backend.dto.MetricasDTO;
import kore.backend.dto.TransacaoDTO;
import kore.backend.dto.MetricasDTO.GastoPorCategoria;
import kore.backend.model.Transacao;
import kore.backend.model.enums.CategoriaTransacao;
import kore.backend.model.enums.TipoTransacao;
import kore.backend.repository.TransacaoRepository;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;

    public TransacaoService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    public Page<Transacao> listarTransacoes(Pageable pageable) {
        return transacaoRepository.findAll(pageable);
    }

    @Transactional
    public Transacao criarTransacao(TransacaoDTO transacaoDTO) {
        Transacao transacao = new Transacao(transacaoDTO);

        if (transacao.getValor() <= 0) {
            throw new RuntimeException("Valor da transação deve ser maior que zero");
        }

        return transacaoRepository.save(transacao);
    }

    public MetricasDTO calcularMetricas() {
        Double totalEntradas = 0.0;
        Double totalSaidas = 0.0;
        Double saldoAtual = 0.0;

        List<Transacao> transacoes = transacaoRepository.findAll();

        for (Transacao transacao : transacoes) {
            if (transacao.getTipo().equals(TipoTransacao.ENTRADA)) {
                totalEntradas += transacao.getValor();
            } else {
                totalSaidas += transacao.getValor();
            }

            saldoAtual = totalEntradas - totalSaidas;
        }

        Map<String, Double> gastosPorCategoria = transacoes.stream()
                .filter(t -> t.getTipo().equals(TipoTransacao.SAIDA))
                .collect(Collectors.groupingBy(t -> t.getCategoria().name(),
                        Collectors.summingDouble(Transacao::getValor)));

        CategoriaTransacao principalGasto = gastosPorCategoria.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .map(CategoriaTransacao::valueOf)
                .orElse(null);

        List<MetricasDTO.GastoPorCategoria> gastos = gastosPorCategoria.entrySet().stream()
                .map(e -> new MetricasDTO.GastoPorCategoria(CategoriaTransacao.valueOf(e.getKey()), e.getValue(),
                        0.0))
                .collect(Collectors.toList());

        List<GastoPorCategoria> gastosCalculados = new ArrayList<>();

        for (GastoPorCategoria gasto : gastos) {
            Double percentual = ((double) gasto.valor() / (double) totalSaidas) * 100;
            System.out.println("Categoria: " + gasto.categoria() + ", Valor: " + gasto.valor() + ", Percentual: "
                    + percentual);

            gastosCalculados.add(new GastoPorCategoria(gasto.categoria(), gasto.valor(), percentual));
        }

        MetricasDTO metricas = new MetricasDTO(totalEntradas, totalSaidas, saldoAtual, principalGasto,
                gastosCalculados);

        return metricas;
    }

    @Transactional
    public void deletarTransacao(Long id) {
        this.transacaoRepository.findById(id).orElseThrow(() -> new RuntimeException("Transação não encontrada"));
        this.transacaoRepository.deleteById(id);
    }

    public Transacao atualizarTransacao(Long id, TransacaoDTO transacaoDTO) {

        Transacao transacao = this.transacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        transacao.setValor(transacaoDTO.valor());
        transacao.setNome(transacaoDTO.nome());
        transacao.setTipo(transacaoDTO.tipo());
        transacao.setCategoria(transacaoDTO.categoria());

        return this.transacaoRepository.save(transacao);
    }
}
