package kore.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kore.backend.exception.AgendamentoNaoEncontradoException;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Agendamento;
import kore.backend.repository.AgendamentoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kore.backend.dto.MetricasDTO;
import kore.backend.dto.TransacaoDTO;
import kore.backend.mapper.TransacaoMapper;
import kore.backend.model.Transacao;
import kore.backend.model.Usuario;
import kore.backend.model.enums.CategoriaTransacao;
import kore.backend.model.enums.StatusAgendamento;
import kore.backend.model.enums.TipoTransacao;
import kore.backend.repository.TransacaoRepository;
import kore.backend.service.validation.TransacaoValidationService;

@Service
public class TransacaoService {

        private final TransacaoRepository transacaoRepository;
        private final AgendamentoRepository agendamentoRepository;
        private final TransacaoValidationService transacaoValidationService;

        public TransacaoService(TransacaoRepository transacaoRepository, AgendamentoRepository agendamentoRepository,
                        TransacaoValidationService transacaoValidationService) {
                this.transacaoRepository = transacaoRepository;
                this.agendamentoRepository = agendamentoRepository;
                this.transacaoValidationService = transacaoValidationService;
        }

        @Transactional
        public Transacao criarTransacao(TransacaoDTO transacaoDTO, Usuario usuario) {
                Transacao transacao = TransacaoMapper.fromDto(transacaoDTO);
                validarValorTransacao(transacao);
                transacao.setUsuario(usuario);
                return transacaoRepository.save(transacao);
        }

        @Transactional
        public Transacao criarTransacao(TransacaoDTO transacaoDTO, Usuario usuario, Long agendamentoId) {
                Transacao transacao = TransacaoMapper.fromDto(transacaoDTO);

                Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                                .orElseThrow(AgendamentoNaoEncontradoException::new);

                validarValorTransacao(transacao);
                transacao.setUsuario(usuario);
                transacao.setSessao(agendamento);

                return transacaoRepository.save(transacao);
        }

        public MetricasDTO calcularMetricas(Usuario usuario) {

                // ── Janelas de tempo ─────────────────────────────────────────────────────
                LocalDate hoje = LocalDate.now();
                LocalDateTime inicioMesAtual = hoje.withDayOfMonth(1).atStartOfDay();
                LocalDateTime inicioProximoMes = inicioMesAtual.plusMonths(1);
                LocalDateTime inicioMesAnterior = inicioMesAtual.minusMonths(1);

                // ── Totais do mês atual via query agregada (sem findAll) ─────────────────
                BigDecimal totalEntradas = Optional.ofNullable(
                                transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                                                usuario, TipoTransacao.ENTRADA, inicioMesAtual, inicioProximoMes))
                                .orElse(BigDecimal.ZERO);

                BigDecimal totalSaidas = Optional.ofNullable(
                                transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                                                usuario, TipoTransacao.SAIDA, inicioMesAtual, inicioProximoMes))
                                .orElse(BigDecimal.ZERO);

                BigDecimal saldoAtual = totalEntradas.subtract(totalSaidas);

                // ── Gastos por categoria do mês atual via query agrupada ─────────────────
                List<Object[]> rawGastos = transacaoRepository
                                .sumSaidaByCategoria(usuario, inicioMesAtual, inicioProximoMes);

                List<MetricasDTO.GastoPorCategoria> gastosPorCategoria = rawGastos.stream()
                                .map(row -> {
                                        CategoriaTransacao categoria = (CategoriaTransacao) row[0];
                                        BigDecimal valor = ((BigDecimal) row[1]);
                                        BigDecimal percentual = totalSaidas.compareTo(BigDecimal.ZERO) > 0
                                                        ? (valor.divide(totalSaidas)).multiply(new BigDecimal(100))
                                                        : BigDecimal.ZERO;
                                        return new MetricasDTO.GastoPorCategoria(categoria, valor, percentual);
                                })
                                .collect(Collectors.toList());

                CategoriaTransacao principalGasto = gastosPorCategoria.stream()
                                .max(Comparator.comparing(MetricasDTO.GastoPorCategoria::valor))
                                .map(MetricasDTO.GastoPorCategoria::categoria)
                                .orElse(null);

                // ── faturamentoBruto = total de entradas do mês atual ────────────────────
                // Semântica: receita bruta realizada no período
                List<Agendamento> agendamentos = this.agendamentoRepository
                                .findByInicioBetweenAndUsuario(inicioMesAtual, inicioProximoMes, usuario);

                BigDecimal faturamentoDeAgendamentos = agendamentos.stream()
                                .filter(a -> !a.getStatus().equals(StatusAgendamento.CANCELADO))
                                .map(a -> a.getPreco() != null ? a.getPreco() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal faturamentoBruto = faturamentoDeAgendamentos;

                // ── Previsão do próximo mês via agendamentos ─────────────────────────────
                LocalDateTime fimProximoMes = inicioProximoMes.plusMonths(1);
                BigDecimal previsaoProximoMes = agendamentoRepository
                                .findByInicioBetween(inicioProximoMes, fimProximoMes)
                                .stream()
                                .map(a -> a.getPreco() != null ? a.getPreco() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // ── Totais do mês anterior para comparação ───────────────────────────────
                BigDecimal entradasMesAnterior = Optional.ofNullable(
                                transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                                                usuario, TipoTransacao.ENTRADA, inicioMesAnterior, inicioMesAtual))
                                .orElse(BigDecimal.ZERO);

                BigDecimal saidasMesAnterior = Optional.ofNullable(
                                transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                                                usuario, TipoTransacao.SAIDA, inicioMesAnterior, inicioMesAtual))
                                .orElse(BigDecimal.ZERO);

                // ── Variações percentuais (Regra 1: variacaoPercentual == variacaoReceita)
                BigDecimal variacaoReceita = calcularVariacao(totalEntradas, entradasMesAnterior);
                BigDecimal variacaoDespesa = calcularVariacao(totalSaidas, saidasMesAnterior);

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

        private void validarValorTransacao(Transacao transacao) {
                transacaoValidationService.validarValor(transacao);
        }

        private BigDecimal calcularVariacao(BigDecimal valorAtual, BigDecimal valorAnterior) {
                if (valorAnterior == null || valorAnterior.compareTo(BigDecimal.ZERO) == 0) {
                        return (valorAtual == null || valorAtual.compareTo(BigDecimal.ZERO) == 0) ? BigDecimal.ZERO : BigDecimal.valueOf(100);
                }
                return valorAtual
                        .subtract(valorAnterior)
                        .divide(valorAnterior, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
        }

        @Transactional
        public void deletarTransacao(Long id) {
                this.transacaoRepository.findById(id)
                                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada", id));
                this.transacaoRepository.deleteById(id);
        }

        public Transacao atualizarTransacao(Long id, TransacaoDTO transacaoDTO) {

                Transacao transacao = this.transacaoRepository.findById(id)
                                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada", id));

                transacao.setValor(transacaoDTO.valor());
                transacao.setNome(transacaoDTO.nome());
                transacao.setTipo(transacaoDTO.tipo());
                transacao.setCategoria(transacaoDTO.categoria());

                if (transacao.getSessao() != null) {
                        transacao.getSessao().setPreco(transacao.getValor());
                        this.agendamentoRepository.save(transacao.getSessao());
                }

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
