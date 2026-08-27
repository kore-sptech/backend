package kore.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // ALTERADO: Importando JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kore.backend.model.Transacao;
import kore.backend.model.Usuario;
import kore.backend.model.enums.TipoTransacao;

import java.time.LocalDateTime;

public interface TransacaoRepository extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao> { // ALTERADO:
                                                                                                                   // Adicionado
                                                                                                                   // JpaSpecificationExecutor

        Page<Transacao> findAll(Pageable pageable);

        List<Transacao> findByUsuario(Usuario usuario);

        List<Transacao> findByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);


        // 1. Soma de valor por tipo e período — elimina o findAll e os streams de soma
        @Query("SELECT SUM(t.valor) FROM Transacao t WHERE t.usuario = :usuario " +
                        "AND t.tipo = :tipo AND t.dataCriacao >= :inicio AND t.dataCriacao < :fim")
        Double sumValorByUsuarioAndTipoAndPeriodo(
                        @Param("usuario") Usuario usuario,
                        @Param("tipo") TipoTransacao tipo,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim);

        // 2. Gastos agrupados por categoria — elimina o groupingBy em memória
        @Query("SELECT t.categoria AS categoria, SUM(t.valor) AS total " +
                        "FROM Transacao t WHERE t.usuario = :usuario " +
                        "AND t.tipo = 'SAIDA' AND t.dataCriacao >= :inicio AND t.dataCriacao < :fim " +
                        "GROUP BY t.categoria")
        List<Object[]> sumSaidaByCategoria(
                        @Param("usuario") Usuario usuario,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim);
}
