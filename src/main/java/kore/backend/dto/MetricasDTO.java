package kore.backend.dto;

import java.util.List;

import kore.backend.model.enums.CategoriaTransacao;

public record MetricasDTO(
                Double totalEntradas,
                Double totalSaidas,
                Double saldoAtual,
                CategoriaTransacao principalGasto,
                List<GastoPorCategoria> gastosPorCategoria) {

        public record GastoPorCategoria(
                        CategoriaTransacao categoria,
                        Double valor, Double percentual) {
        }

        @Override
        public String toString() {
                return "MetricasDTO{" +
                                "totalEntradas=" + totalEntradas +
                                ", totalSaidas=" + totalSaidas +
                                ", saldoAtual=" + saldoAtual +
                                ", principalGasto=" + principalGasto +
                                ", gastosPorCategoria=" + gastosPorCategoria +
                                '}';
        }

}
