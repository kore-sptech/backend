package kore.backend.controller;

import kore.backend.dto.UsuarioDTO;
import kore.backend.model.Usuario;
import kore.backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<Usuario> cadastrar(
             @RequestBody UsuarioDTO usuarioDTO
            ){
        Usuario p = usuarioService.salvar(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(p);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscar(
            @PathVariable Long id){
        Usuario p = usuarioService.buscar(id);
        return ResponseEntity.ok(p);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar
    (
            @PathVariable Long id,
            @RequestBody UsuarioDTO usuarioDTO
            ){
        Usuario p = usuarioService.atualizar(usuarioDTO, id);
        return ResponseEntity.ok(p);
    }
    @DeleteMapping("/{id}")
     public ResponseEntity<Void> deletar(
             @PathVariable Long id
    ){
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
