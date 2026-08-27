package kore.backend.service;

import kore.backend.dto.UsuarioDTO;
import kore.backend.exception.CredencialExistenteException;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Usuario;
import kore.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve salvar um usuário com sucesso quando o e-mail não existir")
    void salvar_ComEmailNovo_SalvaComSucesso() {
        // Arrange
        UsuarioDTO dto = new UsuarioDTO("teste@teste.com", "Vitor", "senha123");

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.senha())).thenReturn("senhaCriptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        // Act
        Usuario resultado = usuarioService.salvar(dto);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("teste@teste.com", resultado.getEmail());
        assertEquals("Vitor", resultado.getNome());
        assertEquals("senhaCriptografada", resultado.getSenha());

        verify(usuarioRepository, times(1)).findByEmail(dto.email());
        verify(passwordEncoder, times(1)).encode(dto.senha());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar CredencialExistenteException ao tentar salvar e-mail já cadastrado")
    void salvar_ComEmailExistente_LancaExcecao() {
        // Arrange
        UsuarioDTO dto = new UsuarioDTO("teste@teste.com", "Vitor", "senha123");
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(new Usuario()));

        // Act & Assert
        assertThrows(CredencialExistenteException.class, () -> usuarioService.salvar(dto));

        verify(usuarioRepository, times(1)).findByEmail(dto.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve buscar um usuário com sucesso quando o ID existir")
    void buscar_ComIdExistente_RetornaUsuario() {
        // Arrange
        Long id = 1L;
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(id);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioMock));

        // Act
        Usuario resultado = usuarioService.buscar(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um usuário com sucesso quando os dados forem válidos")
    void atualizar_ComDadosValidos_AtualizaComSucesso() {
        // Arrange
        Long id = 1L;
        UsuarioDTO dto = new UsuarioDTO("novo@teste.com", "Vitor Atualizado", "novaSenha");

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(id);
        usuarioExistente.setEmail("antigo@teste.com");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty()); // E-mail novo está livre
        when(passwordEncoder.encode(dto.senha())).thenReturn("novaSenhaCriptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario resultado = usuarioService.atualizar(dto, id);

        // Assert
        assertEquals("novo@teste.com", resultado.getEmail());
        assertEquals("Vitor Atualizado", resultado.getNome());
        assertEquals("novaSenhaCriptografada", resultado.getSenha());

        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).findByEmail(dto.email());
        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    @Test
    @DisplayName("Deve lançar CredencialExistenteException ao atualizar usando e-mail de outro usuário")
    void atualizar_ComEmailDeOutroUsuario_LancaExcecao() {
        // Arrange
        Long idOriginal = 1L;
        Long idDeOutroUsuario = 2L;
        UsuarioDTO dto = new UsuarioDTO("usado@teste.com", "Vitor", "senha");

        Usuario usuarioOriginal = new Usuario();
        usuarioOriginal.setId(idOriginal);

        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(idDeOutroUsuario); // Simulando que esse e-mail pertence ao ID 2

        when(usuarioRepository.findById(idOriginal)).thenReturn(Optional.of(usuarioOriginal));
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(outroUsuario));

        // Act & Assert
        assertThrows(CredencialExistenteException.class, () -> usuarioService.atualizar(dto, idOriginal));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve deletar um usuário quando o ID existir")
    void deletar_ComIdExistente_DeletaComSucesso() {
        // Arrange
        Long id = 1L;
        when(usuarioRepository.existsById(id)).thenReturn(true);

        // Act
        usuarioService.deletar(id);

        // Assert
        verify(usuarioRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void buscartodos_RetornaListaDeUsuarios() {
        // Arrange
        List<Usuario> listaMock = List.of(new Usuario(), new Usuario());
        when(usuarioRepository.findAll()).thenReturn(listaMock);

        // Act
        List<Usuario> resultado = usuarioService.buscartodos();

        // Assert
        assertEquals(2, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve realizar login com sucesso com credenciais corretas")
    void login_ComCredenciaisCorretas_RetornaUsuario() {
        // Arrange
        String email = "teste@teste.com";
        String senha = "senha123";

        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail(email);
        usuarioMock.setSenha(senha); // No service atual está usando .equals()

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioMock));

        // Act
        Usuario resultado = usuarioService.login(email, senha);

        // Assert
        assertNotNull(resultado);
        assertEquals(email, resultado.getEmail());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao realizar login com senha incorreta")
    void login_ComSenhaIncorreta_LancaExcecao() {
        // Arrange
        String email = "teste@teste.com";
        String senhaCorreta = "senha123";
        String senhaIncorreta = "senhaErrada";

        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail(email);
        usuarioMock.setSenha(senhaCorreta);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioMock));

        // Act & Assert
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> usuarioService.login(email, senhaIncorreta));

        assertEquals("Senha incorreta", exception.getMessage());
    }
}