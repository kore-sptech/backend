package kore.backend.controller;

import kore.backend.dto.ProdutoDTO;
import kore.backend.model.Produto;
import kore.backend.service.ProdutoService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }
    @PostMapping
    public ResponseEntity<Produto> cadastrar(@RequestBody ProdutoDTO produtoDTO){
        Produto p = produtoService.salvarProduto(produtoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(p);
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listrarProdutos (){
        List<Produto> p = produtoService.listarTodosProdutos();
        return ResponseEntity.ok().body(p);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizarProduto(
            @PathVariable Long id,
            @RequestBody ProdutoDTO produtoDTO
    ){
        Produto p = produtoService.atualizarProduto(id,produtoDTO);
        return ResponseEntity.ok(p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto (@PathVariable Long id){
        produtoService.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }
}
