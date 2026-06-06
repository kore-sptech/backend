package kore.backend.service;

import kore.backend.dto.MetricasDTO;
import kore.backend.model.Transacao;
import kore.backend.model.Agendamento;
import kore.backend.model.enums.TipoTransacao;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.repository.TransacaoRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    @Test
    @DisplayName("Deve calcular totalEntradas corretamente")
    void calcularMetricas_ComEntradas_RetornaTotalCorreto() {
        // Arrange
        Transacao t1 = new Transacao();
        t1.setTipo(TipoTransacao.ENTRADA);
        t1.setValor(300.0);

        Transacao t2 = new Transacao();
        t2.setTipo(TipoTransacao.ENTRADA);
        t2.setValor(200.0);

        when(transacaoRepository.findAll()).thenReturn(List.of(t1, t2));
        when(transacaoRepository.findByDataCriacaoBetween(any(), any())).thenReturn(List.of());
        when(agendamentoRepository.findByInicioBetween(any(), any())).thenReturn(List.of());

        // Act
        MetricasDTO metricas = transacaoService.calcularMetricas();

        // Assert
        assertEquals(500.0, metricas.totalEntradas());
    }

    @Test
    @DisplayName("Deve calcular saldoAtual corretamente")
    void calcularMetricas_ComEntradasESaidas_RetornaSaldoCorreto() {
        // Arrange
        Transacao entrada = new Transacao();
        entrada.setTipo(TipoTransacao.ENTRADA);
        entrada.setValor(500.0);

        Transacao saida = new Transacao();
        saida.setTipo(TipoTransacao.SAIDA);
        saida.setValor(200.0);
        saida.setCategoria(kore.backend.model.enums.CategoriaTransacao.INSUMOS);

        when(transacaoRepository.findAll()).thenReturn(List.of(entrada, saida));
        when(transacaoRepository.findByDataCriacaoBetween(any(), any())).thenReturn(List.of());
        when(agendamentoRepository.findByInicioBetween(any(), any())).thenReturn(List.of());

        // Act
        MetricasDTO metricas = transacaoService.calcularMetricas();

        // Assert
        assertEquals(300.0, metricas.saldoAtual());
    }

    @Test
    @DisplayName("Deve retornar variacaoPercentual null quando não há entradas no mês anterior")
    void calcularMetricas_SemEntradasMesAnterior_RetornaVariacaoNula() {
        // Arrange
        when(transacaoRepository.findAll()).thenReturn(List.of());
        when(transacaoRepository.findByDataCriacaoBetween(any(), any())).thenReturn(List.of());
        when(agendamentoRepository.findByInicioBetween(any(), any())).thenReturn(List.of());

        // Act
        MetricasDTO metricas = transacaoService.calcularMetricas();

        // Assert
        assertNull(metricas.variacaoPercentual());
    }

    @Test
    @DisplayName("Deve calcular previsaoProximoMes somando precos dos agendamentos")
    void calcularMetricas_ComAgendamentosProximoMes_RetornaPrevisaoCorreta() {
        // Arrange
        Agendamento a1 = new Agendamento();
        a1.setPreco(300.0);

        Agendamento a2 = new Agendamento();
        a2.setPreco(200.0);

        when(transacaoRepository.findAll()).thenReturn(List.of());
        when(transacaoRepository.findByDataCriacaoBetween(any(), any())).thenReturn(List.of());
        when(agendamentoRepository.findByInicioBetween(any(), any())).thenReturn(List.of(a1, a2));

        // Act
        MetricasDTO metricas = transacaoService.calcularMetricas();

        // Assert
        assertEquals(500.0, metricas.previsaoProximoMes());
    }
}