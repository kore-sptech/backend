package kore.backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kore.backend.dto.MetricasDTO;
import kore.backend.dto.TransacaoDTO;
import kore.backend.model.enums.CategoriaTransacao;
import kore.backend.model.enums.TipoTransacao;
import kore.backend.model.Transacao;
import kore.backend.model.Usuario;
import kore.backend.service.TransacaoService;

@RestController
@RequestMapping("/transacoes")
@Tag(name = "Transações", description = "Gestão financeira, extratos e métricas")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Operation(summary = "Listar transações", description = "Lista transações paginadas com filtros opcionais de tipo, data e busca")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transações listadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @GetMapping
    public ResponseEntity<Page<Transacao>> listarTransacoes(
            @RequestParam(required = false) Optional<TipoTransacao> tipo,
            @RequestParam(required = false) Optional<LocalDate> dataCriacao,
            @RequestParam(required = false) Optional<String> busca,
            @PageableDefault(size = 4, page = 0, sort = { "id" }, direction = Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Usuario usuario) {
        Page<Transacao> transacoes = transacaoService.buscarTransacoes(tipo, dataCriacao, busca, pageable, usuario);
        return ResponseEntity.ok(transacoes);
    }

    @Operation(summary = "Criar transação", description = "Cria uma nova transação financeira")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<Transacao> criarTransacao(
            @RequestBody TransacaoDTO transacaoDTO,
            @AuthenticationPrincipal Usuario usuario) {

        Transacao transacao = this.transacaoService.criarTransacao(transacaoDTO, usuario);

        return ResponseEntity.ok(transacao);
    }

    @Operation(summary = "Criar transação com agendamento", description = "Cria uma nova transação financeira vinculada a um agendamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PostMapping("/{agendamentoId}")
    public ResponseEntity<Transacao> criarTransacao(
            @RequestBody TransacaoDTO transacaoDTO,
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long agendamentoId) {
        Transacao transacao = this.transacaoService.criarTransacao(transacaoDTO, usuario, agendamentoId);

        return ResponseEntity.ok(transacao);
    }

    @Operation(summary = "Calcular métricas financeiras", description = "Calcula métricas e resumos financeiros para o usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas calculadas com sucesso")
    })
    @GetMapping("/metricas")
    public ResponseEntity<MetricasDTO> calcularMetricas(
            @AuthenticationPrincipal Usuario usuario) {
        MetricasDTO metricas = this.transacaoService.calcularMetricas(usuario);

        return ResponseEntity.ok(metricas);
    }

    @Operation(summary = "Listar gastos por categoria", description = "Lista as categorias de gastos disponíveis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorias listadas com sucesso")
    })
    @GetMapping("/categorias")
    public ResponseEntity<List<String>> listarGastosPorCategoria() {
        return ResponseEntity.ok(List.of(CategoriaTransacao.INSUMOS.name(),
                CategoriaTransacao.MATERIAS.name(), CategoriaTransacao.OUTROS.name()));
    }

    @Operation(summary = "Deletar transação", description = "Remove uma transação financeira pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transação deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTransacao(@PathVariable Long id) {
        transacaoService.deletarTransacao(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar transação", description = "Atualiza os dados de uma transação financeira")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transação atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Transacao> atualizarTransacao(@PathVariable Long id, @RequestBody TransacaoDTO transacaoDTO) {
        Transacao transacaoAtualizada = transacaoService.atualizarTransacao(id, transacaoDTO);
        return ResponseEntity.ok(transacaoAtualizada);
    }

}
