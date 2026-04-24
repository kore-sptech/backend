package kore.backend.controller;

import jakarta.validation.Valid;
import kore.backend.config.security.TokenService;
import kore.backend.dto.LoginDTO;
import kore.backend.dto.LoginResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kore.backend.model.Usuario;
import kore.backend.service.UsuarioService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody  @Valid LoginDTO loginDTO) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        //Usuario usuario = usuarioService.login(loginDTO.email(), loginDTO.senha());

        var token = tokenService.generateToken((Usuario) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

}
