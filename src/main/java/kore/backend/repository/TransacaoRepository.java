package kore.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import kore.backend.model.Transacao;

import java.time.LocalDateTime;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    Page<Transacao> findAll(Pageable pageable);
    List<Transacao> findByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);
}
