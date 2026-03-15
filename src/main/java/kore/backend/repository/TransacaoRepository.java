package kore.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import kore.backend.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    Page<Transacao> findAll(Pageable pageable);

}
