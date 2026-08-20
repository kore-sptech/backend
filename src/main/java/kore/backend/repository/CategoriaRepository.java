package kore.backend.repository;


import kore.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    @Query(value = "SELECT * FROM Categoria WHERE fk_usuario =:fk_usuario", nativeQuery = true)
    Optional<List<Categoria>> buscarPorIdDoUsuario(@Param("fk_usuario") Long id);
    @Query(value = "SELECT * FROM Categoria WHERE fk_usuario =:fk_usuario AND id_categoria=:id_categoria", nativeQuery = true)
    Optional<List<Categoria>> buscarCategoriaPorIdDaCategoriaEDoUsuario(
            @Param("fk_usuario") Long fkUsuario,
            @Param( "id_usuario") Long id);
}
