package kore.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kore.backend.dto.MetricasDTO;
import kore.backend.dto.TransacaoDTO;
import kore.backend.dto.MetricasDTO.GastoPorCategoria;
import kore.backend.model.Transacao;
import kore.backend.model.Usuario;
import kore.backend.model.enums.CategoriaTransacao;
import kore.backend.model.enums.TipoTransacao;
import kore.backend.repository.TransacaoRepository;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;

    public TransacaoService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public Transacao criarTransacao(TransacaoDTO transacaoDTO, Usuario usuario) {
        Transacao transacao = new Transacao(transacaoDTO);

        if (transacao.getValor() <= 0) {
            throw new RuntimeException("Valor da transação deve ser maior que zero");
        }

        transacao.setUsuario(usuario);
        return transacaoRepository.save(transacao);
    }

    public MetricasDTO calcularMetricas(Usuario usuario) {
        Double totalEntradas = 0.0;
        Double totalSaidas = 0.0;
        Double saldoAtual = 0.0;

        List<Transacao> transacoes = transacaoRepository.findByUsuario(usuario);

        // Filtrar apenas transações do mês atual
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        List<Transacao> transacoesDoMes = transacoes.stream()
                .filter(t -> {
                    LocalDateTime dt = t.getDataCriacao();
                    return dt != null && !dt.isBefore(startOfMonth) && dt.isBefore(startOfNextMonth);
                })
                .collect(Collectors.toList());

        for (Transacao transacao : transacoesDoMes) {
            if (transacao.getTipo().equals(TipoTransacao.ENTRADA)) {
                totalEntradas += transacao.getValor();
            } else {
                totalSaidas += transacao.getValor();
            }

            saldoAtual = totalEntradas - totalSaidas;
        }

        Map<String, Double> gastosPorCategoria = transacoesDoMes.stream()
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
            Double percentual = 0.0;
            if (totalSaidas != null && totalSaidas > 0.0) {
                percentual = (gasto.valor() / totalSaidas) * 100;
            }

            System.out.println("Categoria: " + gasto.categoria() + ", Valor: " + gasto.valor() + ", Percentual: "
                    + percentual);

            gastosCalculados.add(new GastoPorCategoria(gasto.categoria(), gasto.valor(), percentual));
        }

        // Calcular métricas do mês passado para comparação
        LocalDateTime startOfPrevMonth = startOfMonth.minusMonths(1);
        LocalDateTime startOfCurrentMonth = startOfMonth;

        List<Transacao> transacoesMesPassado = transacoes.stream()
                .filter(t -> {
                    LocalDateTime dt = t.getDataCriacao();
                    return dt != null && !dt.isBefore(startOfPrevMonth) && dt.isBefore(startOfCurrentMonth);
                })
                .collect(Collectors.toList());

        Double totalEntradasMesPassado = transacoesMesPassado.stream()
                .filter(t -> t.getTipo().equals(TipoTransacao.ENTRADA))
                .mapToDouble(Transacao::getValor)
                .sum();

        Double totalSaidasMesPassado = transacoesMesPassado.stream()
                .filter(t -> t.getTipo().equals(TipoTransacao.SAIDA))
                .mapToDouble(Transacao::getValor)
                .sum();

        Double variacaoReceita = 0.0;
        if (totalEntradasMesPassado == 0.0) {
            variacaoReceita = (totalEntradas == 0.0) ? 0.0 : 100.0;
        } else {
            variacaoReceita = ((totalEntradas - totalEntradasMesPassado) / totalEntradasMesPassado) * 100;
        }

        Double variacaoDespesa = 0.0;
        if (totalSaidasMesPassado == 0.0) {
            variacaoDespesa = (totalSaidas == 0.0) ? 0.0 : 100.0;
        } else {
            variacaoDespesa = ((totalSaidas - totalSaidasMesPassado) / totalSaidasMesPassado) * 100;
        }

        MetricasDTO.MetricasMesPassado mesPassado = new MetricasDTO.MetricasMesPassado(variacaoReceita,
                variacaoDespesa);

        MetricasDTO metricas = new MetricasDTO(totalEntradas, totalSaidas, saldoAtual, principalGasto,
                gastosCalculados, mesPassado);

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

    @Transactional(readOnly = true)
    public Page<Transacao> buscarTransacoes(Optional<TipoTransacao> tipo, Optional<LocalDate> dataCriacao,
            Optional<String> busca, Pageable pageable, Usuario usuario) {
        Specification<Transacao> spec = (root, query, cb) -> cb.equal(root.get("usuario"), usuario);

        if (busca.isPresent()) { // Grupo B
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .like(criteriaBuilder.lower(root.get("nome")), "%" + busca.get().toLowerCase() + "%"));
        }

        if (tipo.isPresent()) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("tipo"), tipo.get()));
        }

        if (dataCriacao.isPresent()) {
            LocalDateTime start = dataCriacao.get().atStartOfDay();
            LocalDateTime end = dataCriacao.get().plusDays(1).atStartOfDay();
            spec = spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("dataCriacao"), start, end));
        }

        return transacaoRepository.findAll(spec, pageable);
    }
}
