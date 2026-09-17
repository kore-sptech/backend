package kore.backend.controller;

import jakarta.validation.Valid;
import kore.backend.dto.CategoriaRequestDTO;
import kore.backend.model.Categoria;
import kore.backend.model.Produto;
import kore.backend.service.CategoriaService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "*", allowedHeaders = "*")
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
        try{
            List<Categoria> categoriaList = categoriaService.listarCategorias(fkUsuario);
            if (categoriaList.isEmpty())
                return ResponseEntity.noContent().build();
            return ResponseEntity.ok().body(categoriaList);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping("/{fkUsuario}/{idCategoria}")
    public ResponseEntity<Categoria> atualizar (
            @PathVariable Long fkUsuario,
            @PathVariable Long idCategoria,
            @RequestBody CategoriaRequestDTO dto
    ){
        try {
            Categoria c = categoriaService.atualizarCategoria(dto, idCategoria, fkUsuario);
            return ResponseEntity.ok(c);
        } catch (Exception e) {
            if (e instanceof DataAccessException)
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
