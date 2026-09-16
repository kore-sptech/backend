package kore.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import kore.backend.config.security.TokenService;
import kore.backend.dto.LoginDTO;
import kore.backend.dto.LoginResponseDTO;
import kore.backend.handler.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import kore.backend.model.Usuario;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    @SecurityRequirements
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {

        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.senha());
            var auth = this.authenticationManager.authenticate(usernamePassword);
            Usuario usuario = (Usuario) auth.getPrincipal();
            var token = tokenService.generateToken(usuario);
            return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getNome()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos", request));
        }
    }

}
