package kore.backend.controller;

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
//@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CategoriaController {
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }
    @PostMapping("/{fkUsuario}")
    public ResponseEntity<Categoria> cadastrar(
            @Valid @RequestBody CategoriaRequestDTO dto,
            @PathVariable Long fkUsuario
            )
    {
        Categoria c = categoriaService.adicionarCategoria(dto, fkUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    @GetMapping("/{fkUsuario}")
    public ResponseEntity<List<Categoria>> listar(@PathVariable Long fkUsuario){
        List<Categoria> categoriaList = categoriaService.listarCategorias(fkUsuario);
        if (categoriaList.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok().body(categoriaList);
    }
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
