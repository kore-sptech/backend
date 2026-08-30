package kore.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kore.backend.dto.produto.ProdutoDTO;
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
    @PostMapping("/{fkUsuario}")
    public ResponseEntity<Produto> cadastrar(
            @Valid @RequestBody ProdutoDTO produtoDTO,
            @PathVariable Long fkUsuario) {
        Produto p = produtoService.salvarProduto(produtoDTO, fkUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(p);
    }

    @Operation(summary = "Listagem de produtos", description = "Lista todos os produtos do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos listados com sucesso"),
            @ApiResponse(responseCode = "204", description = "Sem produtos cadastrados para listar"),
            @ApiResponse(responseCode = "503", description = "Erro ao acessar o banco")
    })
    @GetMapping("/{fkUsuario}")
    public ResponseEntity<List<Produto>> listrarProdutos(
            @PathVariable Long fkUsuario
    ) {
        try {
            // O serviço precisará ser atualizado para receber o fkUsuario
            List<Produto> p = produtoService.listarTodosProdutos(fkUsuario);
            if (p.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok().body(p);
        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                // adicionar logs depois
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Atualização de produto", description = "Atualiza o produto do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "503", description = "Erro ao acessar o banco")
    })
    @PutMapping("/{fkUsuario}/{id}")
    public ResponseEntity<Produto> atualizarProduto(
            @PathVariable Long fkUsuario,
            @PathVariable Long id,
            @Valid @RequestBody ProdutoDTO produtoDTO) {
        try {
            // O serviço precisará ser atualizado para validar/receber o fkUsuario
            Produto p = produtoService.atualizarProduto(fkUsuario, id, produtoDTO);
            return ResponseEntity.ok(p);
        } catch (Exception e) {
            if (e instanceof DataAccessException)
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Remoção de produto", description = "Remove o produto do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "503", description = "Erro ao acessar o banco")
    })
    @DeleteMapping("/{fkUsuario}/{id}")
    public ResponseEntity<Void> deletarProduto(
            @PathVariable Long fkUsuario,
            @PathVariable Long id) {
        try {
            // O serviço precisará ser atualizado para validar/receber o fkUsuario
            produtoService.deletarProduto(fkUsuario, id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e instanceof DataAccessException)
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}