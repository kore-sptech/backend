package kore.backend.controller;

import kore.backend.dto.ItemDTO;
import kore.backend.model.Item;
import kore.backend.model.Produto;
import kore.backend.service.ItemService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/estoque")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Item>> buscarEstoque(@PathVariable Long id) {
        List<Item> estoque = itemService.listarEstoque(id);
        System.out.println(estoque.size());
        if (estoque.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(estoque);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerEstoque(@PathVariable Long id) {
        itemService.removerEstoque(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{qtd}/{id}")
    public ResponseEntity<List<Item>> adicionarEstoque(
            @PathVariable Integer qtd,
            @PathVariable Long id,
            @RequestBody ItemDTO itemDTO) {
        List<Item> e = itemService.adicionarEstoque(itemDTO, qtd, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(e);
    }

    @PutMapping("/{id}/{idAgendamento}")
    public ResponseEntity<Item> atualizarEstoqueComAgendamento(
            @PathVariable Long id,
            @PathVariable Long idAgendamento) {
        try {
            Item i = itemService.atualizarEstoqueComAgendamento(id, idAgendamento);
            return ResponseEntity.ok(i);
        } catch (Exception e) {
            if (e instanceof DataAccessException)
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
