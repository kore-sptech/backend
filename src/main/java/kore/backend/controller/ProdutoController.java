package kore.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kore.backend.dto.ProdutoDTO;
import kore.backend.model.Produto;
import kore.backend.service.ProdutoService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Produto", description = "Operações relacionadas ao CRUD do Produto")
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Operation(summary = "Cadastro de produto", description = "Cadastra um novo produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<Produto> cadastrar(@Valid @RequestBody ProdutoDTO produtoDTO) {
        Produto p = produtoService.salvarProduto(produtoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(p);
    }
    @Operation(summary = "Listagem de produtos", description = "Lista todos os produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos listados com sucesso"),
            @ApiResponse(responseCode = "204", description = "Sem produtos cadastrados para listar"),
            @ApiResponse(responseCode = "503", description = "Erro ao acessar o banco")
    })
    @GetMapping
    public ResponseEntity<List<Produto>> listrarProdutos() {
        try{
            List<Produto> p = produtoService.listarTodosProdutos();
            if (p.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok().body(p);
        }catch (Exception e){
            if (e instanceof DataAccessException){
                // adicionar logs depois
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
    @Operation(summary = "Atualização de produto", description = "Atualiza o produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "503", description = "Erro ao acessar o banco")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizarProduto(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoDTO produtoDTO) {
        try{
            Produto p = produtoService.atualizarProduto(id, produtoDTO);
            return ResponseEntity.ok(p);
        }catch (Exception e){
            if (e instanceof DataAccessException)
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @Operation(summary = "Remoção de produto", description = "Remove o produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "503", description = "Erro ao acessar o banco")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        try{
            produtoService.deletarProduto(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e instanceof DataAccessException)
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
