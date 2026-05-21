package kore.backend.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kore.backend.model.Usuario;
import kore.backend.repository.UsuarioRepository;
import kore.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println(">>> tokenService injetado: " + (tokenService != null ? tokenService.getClass().getSimpleName() : "NULL"));
        var token = this.recoverToken(request);

        System.out.println(">>> Token extraído: [" + token + "]");

        if (token != null) {
            var login = tokenService.validateToken(token);
            if (login != null) {
                UserDetails user = authService.loadUserByUsername(login);

                Optional<Usuario> usuario = this.usuarioRepository.findByEmail(user.getUsername());

                if (usuario.isEmpty()) {
                    System.out.println(">>> Usuário não encontrado para email: " + user.getUsername());
                    throw new RuntimeException("Usuário não encontrado");
                }

                System.out.println("Usuario setado");
                System.out.println(usuario.get());
                var authentication = new UsernamePasswordAuthenticationToken(usuario.get(), null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        System.out.println("Authorization header: " + authHeader);
        if (authHeader == null) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }
}
