package kore.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kore.backend.dto.CategoriaRequestDTO;
import kore.backend.model.Categoria;
import kore.backend.service.CategoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Gerenciamento de categorias de produtos e serviços")
public class CategoriaController {
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }
    @Operation(summary = "Cadastrar categoria", description = "Cadastra uma nova categoria para o usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping("/{fkUsuario}")
    public ResponseEntity<Categoria> cadastrar(
            @Valid @RequestBody CategoriaRequestDTO dto,
            @PathVariable Long fkUsuario
            )
    {
        Categoria c = categoriaService.adicionarCategoria(dto, fkUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    @Operation(summary = "Listar categorias", description = "Lista todas as categorias de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorias listadas com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhuma categoria encontrada")
    })
    @GetMapping("/{fkUsuario}")
    public ResponseEntity<List<Categoria>> listar(@PathVariable Long fkUsuario){
        List<Categoria> categoriaList = categoriaService.listarCategorias(fkUsuario);
        if (categoriaList.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok().body(categoriaList);
    }
    @Operation(summary = "Atualizar categoria", description = "Atualiza uma categoria existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @PutMapping("/{fkUsuario}/{idCategoria}")
    public ResponseEntity<Categoria> atualizar (
            @PathVariable Long fkUsuario,
            @PathVariable Long idCategoria,
            @RequestBody CategoriaRequestDTO dto
    ){
        Categoria c = categoriaService.atualizarCategoria(dto, idCategoria, fkUsuario);
        return ResponseEntity.ok(c);
    }

}
