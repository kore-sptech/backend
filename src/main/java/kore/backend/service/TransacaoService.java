package kore.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kore.backend.repository.AgendamentoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kore.backend.dto.MetricasDTO;
import kore.backend.dto.TransacaoDTO;
import kore.backend.model.Transacao;
import kore.backend.model.Usuario;
import kore.backend.model.enums.CategoriaTransacao;
import kore.backend.model.enums.TipoTransacao;
import kore.backend.repository.TransacaoRepository;

@Service
public class TransacaoService {

        private final TransacaoRepository transacaoRepository;
        private final AgendamentoRepository agendamentoRepository;

        public TransacaoService(TransacaoRepository transacaoRepository, AgendamentoRepository agendamentoRepository) {
                this.transacaoRepository = transacaoRepository;
                this.agendamentoRepository = agendamentoRepository;
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

                // ── Janelas de tempo ─────────────────────────────────────────────────────
                LocalDate hoje = LocalDate.now();
                LocalDateTime inicioMesAtual = hoje.withDayOfMonth(1).atStartOfDay();
                LocalDateTime inicioProximoMes = inicioMesAtual.plusMonths(1);
                LocalDateTime inicioMesAnterior = inicioMesAtual.minusMonths(1);

                // ── Totais do mês atual via query agregada (sem findAll) ─────────────────
                Double totalEntradas = Optional.ofNullable(
                                transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                                                usuario, TipoTransacao.ENTRADA, inicioMesAtual, inicioProximoMes))
                                .orElse(0.0);

                Double totalSaidas = Optional.ofNullable(
                                transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                                                usuario, TipoTransacao.SAIDA, inicioMesAtual, inicioProximoMes))
                                .orElse(0.0);

                Double saldoAtual = totalEntradas - totalSaidas;

                // ── Gastos por categoria do mês atual via query agrupada ─────────────────
                List<Object[]> rawGastos = transacaoRepository
                                .sumSaidaByCategoria(usuario, inicioMesAtual, inicioProximoMes);

                List<MetricasDTO.GastoPorCategoria> gastosPorCategoria = rawGastos.stream()
                                .map(row -> {
                                        CategoriaTransacao categoria = (CategoriaTransacao) row[0];
                                        Double valor = ((Number) row[1]).doubleValue();
                                        Double percentual = totalSaidas > 0.0
                                                        ? (valor / totalSaidas) * 100
                                                        : 0.0;
                                        return new MetricasDTO.GastoPorCategoria(categoria, valor, percentual);
                                })
                                .collect(Collectors.toList());

                CategoriaTransacao principalGasto = gastosPorCategoria.stream()
                                .max(Comparator.comparingDouble(MetricasDTO.GastoPorCategoria::valor))
                                .map(MetricasDTO.GastoPorCategoria::categoria)
                                .orElse(null);

                // ── faturamentoBruto = total de entradas do mês atual ────────────────────
                // Semântica: receita bruta realizada no período
                Double faturamentoBruto = totalEntradas; 

                // ── Previsão do próximo mês via agendamentos ─────────────────────────────
                LocalDateTime fimProximoMes = inicioProximoMes.plusMonths(1);
                Double previsaoProximoMes = agendamentoRepository
                                .findByInicioBetween(inicioProximoMes, fimProximoMes)
                                .stream()
                                .mapToDouble(a -> a.getPreco() != null ? a.getPreco() : 0.0)
                                .sum();

                // ── Totais do mês anterior para comparação ───────────────────────────────
                Double entradasMesAnterior = Optional.ofNullable(
                                transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                                                usuario, TipoTransacao.ENTRADA, inicioMesAnterior, inicioMesAtual))
                                .orElse(0.0);

                Double saidasMesAnterior = Optional.ofNullable(
                                transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                                                usuario, TipoTransacao.SAIDA, inicioMesAnterior, inicioMesAtual))
                                .orElse(0.0);

                // ── Variações percentuais (Regra 1: variacaoPercentual == variacaoReceita)
                Double variacaoReceita = calcularVariacao(totalEntradas, entradasMesAnterior);
                Double variacaoDespesa = calcularVariacao(totalSaidas, saidasMesAnterior);

                MetricasDTO.MetricasMesPassado mesPassado = new MetricasDTO.MetricasMesPassado(variacaoReceita,
                                variacaoDespesa);

                return new MetricasDTO(
                                totalEntradas,
                                totalSaidas,
                                saldoAtual,
                                principalGasto,
                                gastosPorCategoria,
                                faturamentoBruto,
                                previsaoProximoMes,
                                variacaoReceita,
                                mesPassado);
        }

        private Double calcularVariacao(Double valorAtual, Double valorAnterior) {
                if (valorAnterior == null || valorAnterior == 0.0) {
                        return (valorAtual == null || valorAtual == 0.0) ? 0.0 : 100.0;
                }
                return ((valorAtual - valorAnterior) / valorAnterior) * 100;
        }

        @Transactional
        public void deletarTransacao(Long id) {
                this.transacaoRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
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
                                        .like(criteriaBuilder.lower(root.get("nome")),
                                                        "%" + busca.get().toLowerCase() + "%"));
                }

                if (tipo.isPresent()) {
                        spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("tipo"),
                                        tipo.get()));
                }

                if (dataCriacao.isPresent()) {
                        LocalDateTime start = dataCriacao.get().atStartOfDay();
                        LocalDateTime end = dataCriacao.get().plusDays(1).atStartOfDay();
                        spec = spec.and(
                                        (root, query, criteriaBuilder) -> criteriaBuilder
                                                        .between(root.get("dataCriacao"), start, end));
                }

                return transacaoRepository.findAll(spec, pageable);
        }
}
