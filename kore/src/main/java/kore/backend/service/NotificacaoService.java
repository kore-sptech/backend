package kore.backend.service;

import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Notificacao;
import kore.backend.model.Usuario;
import kore.backend.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public List<Notificacao> listar(Usuario usuario) {
        if (usuario == null) {
            return Collections.emptyList();
        }
        return notificacaoRepository.findByUsuario(usuario);
    }

    @Transactional
    public Notificacao marcarComoLida(Long id, Usuario usuario) {
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Notificação não encontrada: " + id, id);
        }
        Notificacao notificacao = notificacaoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada: " + id, id));
        notificacao.setLida(true);
        return notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void ignorar(Long id, Usuario usuario) {
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Notificação não encontrada: " + id, id);
        }
        Notificacao notificacao = notificacaoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada: " + id, id));
        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    public Map<String, Long> resumo(Usuario usuario) {
        if (usuario == null) {
            return Map.of(
                    "CRITICO", 0L,
                    "ATENCAO", 0L,
                    "INFORMATIVO", 0L
            );
        }
        List<Notificacao> notificacoes = notificacaoRepository.findByUsuario(usuario);
        long critico = notificacoes.stream()
                .filter(n -> "CRITICO".equalsIgnoreCase(n.getSeveridade()))
                .count();
        long atencao = notificacoes.stream()
                .filter(n -> "ATENCAO".equalsIgnoreCase(n.getSeveridade()))
                .count();
        long informativo = notificacoes.stream()
                .filter(n -> "INFORMATIVO".equalsIgnoreCase(n.getSeveridade()) || n.getSeveridade() == null)
                .count();

        return Map.of(
                "CRITICO", critico,
                "ATENCAO", atencao,
                "INFORMATIVO", informativo
        );
    }
}
