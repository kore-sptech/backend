package kore.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kore.backend.dto.ItemDTO;
import kore.backend.dto.ItemEstoqueResponseDTO;
import kore.backend.model.Item;
import kore.backend.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/estoque")
@Tag(name = "Estoque", description = "Gerenciamento de estoque e itens vinculados")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(summary = "Buscar estoque", description = "Lista todos os itens de estoque pelo ID do usuário/estabelecimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque listado com sucesso"),
            @ApiResponse(responseCode = "204", description = "Estoque vazio")
    })
    @GetMapping("/{id}")
    public ResponseEntity<List<Item>> buscarEstoque(@PathVariable Long id) {
        List<Item> estoque = itemService.listarEstoque(id);
        if (estoque.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(estoque);
    }
    @Operation(summary = "Buscar estoque por agendamento", description = "Lista os itens de estoque vinculados a um agendamento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Itens encontrados com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhum item encontrado")
    })
    @GetMapping("/agendamento/{id}")
    public ResponseEntity<List<ItemEstoqueResponseDTO>> buscarEstoquePorAgendamento(@PathVariable Long id){
        List<ItemEstoqueResponseDTO> estoque = itemService.buscarItensPorAgendamento(id);
        if (estoque.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(estoque);
    }

    @Operation(summary = "Desassociar estoque de agendamento", description = "Remove o vínculo entre o item de estoque e o agendamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Desassociado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @DeleteMapping("/{id}/agendamento")
    public ResponseEntity<Item> desassociarEstoqueDeAgendamento(@PathVariable Long id) {
        Item item = itemService.desassociarEstoqueDeAgendamento(id);
        return ResponseEntity.ok(item);
    }
    @Operation(summary = "Remover item de estoque", description = "Remove um item do estoque pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerEstoque(@PathVariable Long id) {
        itemService.removerEstoque(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Adicionar ao estoque", description = "Adiciona uma quantidade de itens ao estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping("/{qtd}/{id}")
    public ResponseEntity<List<Item>> adicionarEstoque(
            @PathVariable Integer qtd,
            @PathVariable Long id,
            @RequestBody ItemDTO itemDTO) {
        List<Item> e = itemService.adicionarEstoque(itemDTO, qtd, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(e);
    }

    @Operation(summary = "Atualizar estoque com agendamento", description = "Vincula ou atualiza o estoque com base em um agendamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item ou agendamento não encontrado")
    })
    @PutMapping("/{id}/{idAgendamento}")
    public ResponseEntity<Item> atualizarEstoqueComAgendamento(
            @PathVariable Long id,
            @PathVariable Long idAgendamento) {
        Item i = itemService.atualizarEstoqueComAgendamento(id, idAgendamento);
        return ResponseEntity.ok(i);
    }

}
