package kore.backend.repository;

import kore.backend.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findAllByFkUsuario(Long fkUsuario);

    Optional<Produto> findByIdAndFkUsuario(Long id, Long fkUsuario);
}
