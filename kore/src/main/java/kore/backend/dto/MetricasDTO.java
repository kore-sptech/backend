package kore.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import kore.backend.model.enums.CategoriaTransacao;

public record MetricasDTO(
                BigDecimal totalEntradas,
                BigDecimal totalSaidas,
                BigDecimal saldoAtual,
                CategoriaTransacao principalGasto,
                List<GastoPorCategoria> gastosPorCategoria,
                BigDecimal faturamentoBruto,
                BigDecimal previsaoProximoMes,
                BigDecimal variacaoPercentual,
                MetricasMesPassado mesPassado) {

        public record MetricasMesPassado(
                        BigDecimal variacaoReceita,
                        BigDecimal variacaoDespesa

        ) {
        }

        public record GastoPorCategoria(
                        CategoriaTransacao categoria,
                        BigDecimal valor, BigDecimal percentual) {
        }

        @Override
        public String toString() {
                return "MetricasDTO{" +
                                "totalEntradas=" + totalEntradas +
                                ", totalSaidas=" + totalSaidas +
                                ", saldoAtual=" + saldoAtual +
                                ", principalGasto=" + principalGasto +
                                ", gastosPorCategoria=" + gastosPorCategoria +
                                ", faturamentoBruto=" + faturamentoBruto +
                                ", previsaoProximoMes=" + previsaoProximoMes +
                                ", variacaoPercentual=" + variacaoPercentual +
                                '}';
        }

}
