package kore.backend.repository;

import kore.backend.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Page<Produto> findAllByFkUsuario(Long fkUsuario, Pageable pageable);

    Optional<Produto> findByIdAndFkUsuario(Long id, Long fkUsuario);
}
