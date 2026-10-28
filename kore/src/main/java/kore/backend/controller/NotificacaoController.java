package kore.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kore.backend.config.OpenApiConfig;
import kore.backend.model.Notificacao;
import kore.backend.model.Usuario;
import kore.backend.repository.NotificacaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
@Tag(name = "Notificações", description = "Gestão, consulta e ações interativas das notificações do estúdio")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEMA_NAME)
public class NotificacaoController {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoController(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Operation(summary = "Listar notificações", description = "Lista todas as notificações registradas no sistema para o tatuador logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificações listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado (JWT ausente ou inválido)")
    })
    @GetMapping
    public ResponseEntity<List<Notificacao>> listar(@AuthenticationPrincipal Usuario usuario) {
        List<Notificacao> notificacoes = notificacaoRepository.findAll();
        return ResponseEntity.ok(notificacoes);
    }

    @Operation(summary = "Marcar notificação como lida", description = "Atualiza o status de leitura de uma notificação específica para true")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificação marcada como lida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PatchMapping("/{id}/lida")
    public ResponseEntity<Notificacao> marcarComoLida(
            @Parameter(description = "ID da notificação", required = true) @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada: " + id));
        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
        return ResponseEntity.ok(notificacao);
    }

    @Operation(summary = "Ignorar / Dispensar notificação", description = "Marca a notificação como lida/arquivada (ignorada pelo usuário)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificação ignorada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PatchMapping("/{id}/ignorar")
    public ResponseEntity<Void> ignorar(
            @Parameter(description = "ID da notificação", required = true) @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada: " + id));
        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Resumo de alertas", description = "Retorna a contagem de notificações agrupadas por severidade (CRITICO, ATENCAO, INFORMATIVO)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo obtido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Long>> resumo(@AuthenticationPrincipal Usuario usuario) {
        List<Notificacao> todas = notificacaoRepository.findAll();
        long critico = todas.stream().filter(n -> "CRITICO".equalsIgnoreCase(n.getSeveridade())).count();
        long atencao = todas.stream().filter(n -> "ATENCAO".equalsIgnoreCase(n.getSeveridade())).count();
        long informativo = todas.stream().filter(n -> "INFORMATIVO".equalsIgnoreCase(n.getSeveridade()) || n.getSeveridade() == null).count();

        Map<String, Long> resumoMap = Map.of(
                "CRITICO", critico,
                "ATENCAO", atencao,
                "INFORMATIVO", informativo
        );
        return ResponseEntity.ok(resumoMap);
    }
}
