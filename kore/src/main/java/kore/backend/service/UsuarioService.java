package kore.backend.service;

import kore.backend.model.Categoria;
import kore.backend.repository.CategoriaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import kore.backend.dto.UsuarioDTO;
import kore.backend.model.Usuario;
import kore.backend.repository.UsuarioRepository;
import kore.backend.service.policy.UsuarioPolicy;
import kore.backend.service.validation.UsuarioValidationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioValidationService usuarioValidationService;
    private final UsuarioPolicy usuarioPolicy;
    private final CategoriaRepository categoriaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                         UsuarioValidationService usuarioValidationService, UsuarioPolicy usuarioPolicy,
                         CategoriaRepository categoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioValidationService = usuarioValidationService;
        this.usuarioPolicy = usuarioPolicy;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public Usuario salvar(UsuarioDTO usuarioDTO) {
        usuarioPolicy.validarCadastro(usuarioDTO);

        Usuario p = new Usuario();
        p.setEmail(usuarioDTO.email());
        p.setNome(usuarioDTO.nome());
        p.setSenha(passwordEncoder.encode(usuarioDTO.senha()));

        Usuario usuarioSalvo = usuarioRepository.save(p);

        // Criar categorias padrão para o novo usuário
        criarCategoriasPadrao(usuarioSalvo.getId());

        return usuarioSalvo;
    }

    private void criarCategoriasPadrao(Long usuarioId) {
        String[][] categoriasPadrao = {
            {"Tintas", "Tintas de tatuagem"},
            {"Agulhas", "Agulhas descartáveis"},
            {"Luvas", "Luvas descartáveis"},
            {"Biqueiras", "Biqueiras/tips descartáveis"},
            {"Limpeza", "Material de limpeza e esterilização"},
            {"Equipamentos", "Máquinas, fontes, cabos"}
        };

        for (String[] cat : categoriasPadrao) {
            Categoria categoria = new Categoria(cat[0], cat[1], usuarioId);
            categoriaRepository.save(categoria);
        }
    }

    public Usuario buscar(Long id) {
        return usuarioValidationService.buscarOuLancar(id);
    }

    @Transactional
    public Usuario atualizar(UsuarioDTO usuarioDTO, Long id) {
        Usuario usuario = usuarioValidationService.buscarOuLancar(id);
        usuarioPolicy.validarAtualizacao(usuarioDTO, id);

        usuario.setEmail(usuarioDTO.email());
        usuario.setSenha(passwordEncoder.encode(usuarioDTO.senha()));
        usuario.setNome(usuarioDTO.nome());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void deletar(Long id) {
        usuarioPolicy.validarExistencia(id);
        usuarioRepository.deleteById(id);
    }

    public List<Usuario> buscartodos() {
        return this.usuarioRepository.findAll();
    }

    public Usuario login(String email, String senha) {
        return usuarioPolicy.validarLogin(email, senha);
    }
}
