package kore.backend.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kore.backend.model.Usuario;
import kore.backend.repository.UsuarioRepository;
import kore.backend.service.AuthService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, AuthService authService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = recoverToken(request);

        if (token != null) {
            String login = tokenService.validateToken(token);
            if (login != null) {
                authenticate(login);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String login) {
        UserDetails user = authService.loadUserByUsername(login);
        Usuario usuario = usuarioRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        return authHeader.replace("Bearer ", "").trim();
    }
}
