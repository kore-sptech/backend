package kore.backend.controller;

import jakarta.validation.Valid;
import kore.backend.config.security.TokenService;
import kore.backend.dto.LoginDTO;
import kore.backend.dto.LoginResponseDTO;
import kore.backend.model.Usuario;
import kore.backend.service.AuthService;
import kore.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthService authService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDTO loginDTO) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        Usuario usuario = (Usuario) auth.getPrincipal();
        var token = tokenService.generateToken(usuario);
        var refreshToken = tokenService.generateRefreshToken(usuario);

        return ResponseEntity.ok(new LoginResponseDTO(token, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body("refreshToken é obrigatório");
        }

        String email = tokenService.validateRefreshToken(refreshToken);

        if (email == null) {
            return ResponseEntity.status(401).body("Refresh token inválido ou expirado");
        }

        Usuario usuario = (Usuario) authService.loadUserByUsername(email);
        var novoToken = tokenService.generateToken(usuario);
        var novoRefreshToken = tokenService.generateRefreshToken(usuario);

        return ResponseEntity.ok(new LoginResponseDTO(novoToken, novoRefreshToken));
    }
}