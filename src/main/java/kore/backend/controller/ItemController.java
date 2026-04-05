package kore.backend.controller;

import kore.backend.dto.ItemDTO;
import kore.backend.model.Item;
import kore.backend.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/estoque")
public class ItemController {
    private final ItemService itemService;
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<List<Item>> buscarEstoque(@PathVariable Long id){
        List<Item> estoque = itemService.listarEstoque(id);
        if (estoque.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(estoque);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerEstoque(@PathVariable Long id){
        itemService.removerEstoque(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{qtd}/{id}")
    public ResponseEntity<List<Item>> adicionarEstoque(
            @PathVariable Integer qtd,
            @PathVariable Long id,
            @RequestBody ItemDTO itemDTO
            ){
        List<Item> e =  itemService.adicionarEstoque(itemDTO, qtd, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(e);
    }
}
