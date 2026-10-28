package kore.backend.repository;

import kore.backend.model.Agendamento;
import kore.backend.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query(value = "SELECT * FROM item WHERE fk_produto = :id_produto", nativeQuery = true)
    Optional<List<Item>> buscarPorIdDoProduto(@Param("id_produto") Long id);

    @Query("SELECT i FROM Item i JOIN FETCH i.produto WHERE i.agendamento.id = :idAgendamento")
    List<Item> buscarPorIdDoAgendamento(@Param("idAgendamento") Long idAgendamento);

    @Query("select count(i) from Item i where i.produto.id = :produtoId and i.seAtivo = true")
    long contarItensAtivosPorProduto(@Param("produtoId") Long produtoId);

    @Query("select i from Item i join fetch i.produto where i.dataValidade between :inicio and :fim and i.seAtivo = true")
    List<Item> findByDataValidadeBetweenAndSeAtivo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, @Param("seAtivo") boolean seAtivo);

    @Query("select i from Item i join fetch i.produto where i.dataValidade < :dataLimite and i.seAtivo = true")
    List<Item> findByDataValidadeBeforeAndSeAtivo(@Param("dataLimite") LocalDateTime dataLimite, @Param("seAtivo") boolean seAtivo);

    long deleteAllByAgendamento(Agendamento agendamento);
}
