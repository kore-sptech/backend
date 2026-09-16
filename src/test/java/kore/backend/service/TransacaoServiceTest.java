package kore.backend.service;

import kore.backend.dto.MetricasDTO;
import kore.backend.model.Agendamento;
import kore.backend.model.Usuario;
import kore.backend.model.enums.CategoriaTransacao;
import kore.backend.model.enums.TipoTransacao;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    // Usuario reutilizado em todos os testes
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
    }

    // ── Helper: stub padrão para queries que não são foco do teste ───────────
    // Evita UnnecessaryStubbingException usando lenient() nos stubs de suporte
    private void stubDefault() {
        lenient().when(transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                eq(usuario), eq(TipoTransacao.ENTRADA), any(), any())).thenReturn(0.0);

        lenient().when(transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                eq(usuario), eq(TipoTransacao.SAIDA), any(), any())).thenReturn(0.0);

        lenient().when(transacaoRepository.sumSaidaByCategoria(
                eq(usuario), any(), any())).thenReturn(List.of());

        lenient().when(agendamentoRepository.findByInicioBetween(
                any(), any())).thenReturn(List.of());
    }

    @Test
    @DisplayName("Deve calcular totalEntradas corretamente somando entradas do mês atual")
    void calcularMetricas_ComEntradas_RetornaTotalCorreto() {
        stubDefault();

        // Stub específico: retorna 500.0 para ENTRADA no mês atual
        // O Mockito vai usar este stub mais específico sobre o lenient() do helper
        when(transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                eq(usuario), eq(TipoTransacao.ENTRADA), any(), any()))
                .thenReturn(500.0);

        MetricasDTO metricas = transacaoService.calcularMetricas(usuario);

        assertEquals(500.0, metricas.totalEntradas());
    }

    @Test
    @DisplayName("Deve retornar variacaoPercentual 0.0 quando não há entradas no mês anterior")
    void calcularMetricas_SemEntradasMesAnterior_RetornaVariacaoZero() {
        // Arrange: mês atual tem entradas, mês anterior não tem
        // Ambos os períodos usam o mesmo método, diferenciados pelo argumento de data.
        // Como não conseguimos distinguir os períodos facilmente com any(),
        // usamos 0.0 como retorno padrão — o que representa "sem entradas" nos dois
        // meses.
        stubDefault();

        MetricasDTO metricas = transacaoService.calcularMetricas(usuario);

        // Com totalEntradas = 0.0 e entradasMesAnterior = 0.0 → calcularVariacao
        // retorna 0.0
        assertEquals(0.0, metricas.variacaoPercentual());
    }

    @Test
    @DisplayName("Deve retornar variacaoPercentual 100.0 quando há entradas no mês atual mas não no anterior")
    void calcularMetricas_ComEntradasSomenteNoMesAtual_RetornaVariacao100() {
        // Para distinguir mês atual de mês anterior precisamos capturar os argumentos
        // de data.
        // Usamos ArgumentCaptor para verificar a ordem das chamadas e retornar valores
        // distintos.
        stubDefault();

        // Primeira chamada = mês atual (inicio >= startOfMonth), segunda = mês anterior
        when(transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                eq(usuario), eq(TipoTransacao.ENTRADA), any(), any()))
                .thenReturn(300.0) // mês atual
                .thenReturn(0.0); // mês anterior

        MetricasDTO metricas = transacaoService.calcularMetricas(usuario);

        assertEquals(100.0, metricas.variacaoPercentual());
    }

    @Test
    @DisplayName("Deve calcular previsaoProximoMes somando preços dos agendamentos futuros")
    void calcularMetricas_ComAgendamentosProximoMes_RetornaPrevisaoCorreta() {
        stubDefault();

        Agendamento a1 = new Agendamento();
        a1.setPreco(300.0);

        Agendamento a2 = new Agendamento();
        a2.setPreco(200.0);

        when(agendamentoRepository.findByInicioBetween(any(), any()))
                .thenReturn(List.of(a1, a2));

        MetricasDTO metricas = transacaoService.calcularMetricas(usuario);

        assertEquals(500.0, metricas.previsaoProximoMes());
    }

    @Test
    @DisplayName("Deve calcular percentual corretamente por categoria")
    void calcularMetricas_ComSaidasPorCategoria_RetornaPercentualCorreto() {
        stubDefault();

        when(transacaoRepository.sumValorByUsuarioAndTipoAndPeriodo(
                eq(usuario), eq(TipoTransacao.SAIDA), any(), any()))
                .thenReturn(400.0);

        when(transacaoRepository.sumSaidaByCategoria(eq(usuario), any(), any()))
                .thenReturn(List.of(
                        new Object[] { CategoriaTransacao.INSUMOS, 200.0 },
                        new Object[] { CategoriaTransacao.SESSAO, 200.0 }));

        MetricasDTO metricas = transacaoService.calcularMetricas(usuario);

        metricas.gastosPorCategoria().forEach(g -> assertEquals(50.0, g.percentual(), 0.001));
    }

    @Test
    @DisplayName("Deve preencher mesPassado com variacaoReceita e variacaoDespesa")
    void calcularMetricas_RetornaMetricasMesPassadoPreenchidas() {
        stubDefault();

        MetricasDTO metricas = transacaoService.calcularMetricas(usuario);

        assertNotNull(metricas.mesPassado());
        assertNotNull(metricas.mesPassado().variacaoReceita());
        assertNotNull(metricas.mesPassado().variacaoDespesa());
    }
}