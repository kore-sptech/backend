package kore.backend.controller;

import jakarta.validation.Valid;
import kore.backend.dto.UsuarioDTO;
import kore.backend.dto.response.UsuarioResponseDTO;
import kore.backend.mapper.UsuarioMapper;
import kore.backend.model.Usuario;
import kore.backend.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Gerenciamento de cadastro e perfis de usuários")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Cadastrar usuário", description = "Cadastra um novo usuário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    @SecurityRequirements
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody @Valid UsuarioDTO usuarioDTO) {
        Usuario p = usuarioService.salvar(usuarioDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioMapper.toResponse(p));
    }

    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados do usuário pelo ID (requer autenticação e que seja o próprio usuário)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscar(
            @PathVariable Long id, Authentication authentication) {

        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        if (!usuarioLogado.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Usuario p = usuarioService.buscar(id);
        return ResponseEntity.ok(UsuarioMapper.toResponse(p));
    }

    @Operation(summary = "Listar todos os usuários", description = "Retorna uma lista com todos os usuários cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários listados com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhum usuário cadastrado")
    })
    @GetMapping()
    public ResponseEntity<List<UsuarioResponseDTO>> buscartodos() {
        List<Usuario> p = usuarioService.buscartodos();
        return ResponseEntity.ok(UsuarioMapper.toResponseList(p));
    }

    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody UsuarioDTO usuarioDTO,
            Authentication authentication) {

        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        if (!usuarioLogado.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Usuario p = usuarioService.atualizar(usuarioDTO, id);
        return ResponseEntity.ok(UsuarioMapper.toResponse(p));
    }

    @Operation(summary = "Deletar usuário", description = "Remove um usuário do sistema pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        if (!usuarioLogado.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
