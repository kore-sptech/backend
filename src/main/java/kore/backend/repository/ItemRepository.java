package kore.backend.repository;

import kore.backend.model.Agendamento;
import kore.backend.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query(value = "SELECT * FROM item WHERE fk_produto = :id_produto", nativeQuery = true)
    Optional<List<Item>> buscarPorIdDoProduto(@Param("id_produto") Long id);

    @Query(value = "SELECT * FROM item WHERE fk_sessao = :id_sessao", nativeQuery = true)
    Optional<List<Item>> buscarPorIdDoAgendamento(@Param("id_sessao") Long id);
    long deleteAllByAgendamento(Agendamento agendamento);
}
