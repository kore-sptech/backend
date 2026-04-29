package kore.backend.controller;

import jakarta.validation.Valid;
import kore.backend.dto.UsuarioDTO;
import kore.backend.dto.response.UsuarioResponseDTO;
import kore.backend.mapper.UsuarioMapper;
import kore.backend.model.Usuario;
import kore.backend.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @SecurityRequirements
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody @Valid UsuarioDTO usuarioDTO) {
        Usuario p = usuarioService.salvar(usuarioDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioMapper.toResponse(p));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscar(
            @PathVariable Long id) {
        Usuario p = usuarioService.buscar(id);
        return ResponseEntity.ok(UsuarioMapper.toResponse(p));
    }

    @GetMapping()
    public ResponseEntity<List<UsuarioResponseDTO>> buscartodos() {
        List<Usuario> p = usuarioService.buscartodos();
        return ResponseEntity.ok(UsuarioMapper.toResponseList(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody UsuarioDTO usuarioDTO) {
        Usuario p = usuarioService.atualizar(usuarioDTO, id);
        return ResponseEntity.ok(UsuarioMapper.toResponse(p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
