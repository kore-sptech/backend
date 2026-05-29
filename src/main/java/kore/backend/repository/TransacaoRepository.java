package kore.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // ALTERADO: Importando JpaSpecificationExecutor

import kore.backend.model.Transacao;
import kore.backend.model.Usuario;

public interface TransacaoRepository extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao> { // ALTERADO:
                                                                                                                   // Adicionado
                                                                                                                   // JpaSpecificationExecutor

    Page<Transacao> findAll(Pageable pageable);

    Page<Transacao> findByUsuario(Specification<Transacao> spec, Pageable pageable, Usuario usuario);

    List<Transacao> findByUsuario(Usuario usuario);
}
