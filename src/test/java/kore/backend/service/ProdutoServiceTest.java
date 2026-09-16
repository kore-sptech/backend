package kore.backend.service;

import kore.backend.dto.produto.ProdutoDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Produto;
import kore.backend.repository.CategoriaRepository;
import kore.backend.repository.ProdutoRepository;
import kore.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    @DisplayName("Deve salvar um produto com sucesso")
    void salvarProduto_ComDadosValidos_RetornaProdutoSalvo() {
        // Arrange (Preparar)
        Long fkUsuario = 1L;

        ProdutoDTO dto = new ProdutoDTO("Teclado", "Teclado Mecânico", false, 10, null, null);
        Produto produtoSalvo = new Produto(dto, fkUsuario);

        // Assumindo que a entidade geraria um ID no banco
        produtoSalvo.setId(1L);

        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoSalvo);

        // Act (Agir)
        Produto resultado = produtoService.salvarProduto(dto, 1L);

        // Assert (Garantir/Verificar)
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Teclado", resultado.getNome());
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void listarTodosProdutos_DeveRetornarListaDeProdutos() {
        // Arrange
        Long fkUsuario = 1L;

        ProdutoDTO dto1 = new ProdutoDTO("Teclado", "Teclado Mecânico", false, 10, null, null);
        ProdutoDTO dto2 = new ProdutoDTO("Mouse", "Mouse Gamer", false, 5, null, null);

        List<Produto> listaMock = List.of(
                new Produto(dto1, fkUsuario),
                new Produto(dto2, fkUsuario)
        );

        when(produtoRepository.findAll()).thenReturn(listaMock);

        // Act
        List<Produto> resultado = produtoService.listarTodosProdutos(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(produtoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar um produto quando o ID existir")
    void atualizarProduto_ComIdExistente_RetornaProdutoAtualizado() {
        // Arrange
        Long idExistente = 1L;

        ProdutoDTO dtoAtualizacao = new ProdutoDTO(
                "Monitor",
                "Monitor 144hz",
                false,
                20,
                null,
                null
        );

        Produto produtoExistente = new Produto();
        produtoExistente.setId(idExistente);
        produtoExistente.setNome("Monitor Antigo");

        when(produtoRepository.findById(idExistente))
                .thenReturn(Optional.of(produtoExistente));

        when(produtoRepository.save(any(Produto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Produto resultado = produtoService.atualizarProduto(
                1L,
                idExistente,
                dtoAtualizacao
        );

        // Assert
        assertNotNull(resultado);
        assertEquals("Monitor", resultado.getNome());
        assertEquals("Monitor 144hz", resultado.getDescricao());
        assertEquals(20, resultado.getQtdMinAlerta());

        verify(produtoRepository, times(1)).findById(idExistente);
        verify(produtoRepository, times(1)).save(produtoExistente);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar atualizar produto inexistente")
    void atualizarProduto_ComIdInexistente_LancaExcecao() {
        // Arrange
        Long idInexistente = 99L;

        ProdutoDTO dtoAtualizacao = new ProdutoDTO(
                "Monitor",
                "Monitor 144hz",
                false,
                20,
                null,
                null
        );

        when(produtoRepository.findById(idInexistente))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.atualizarProduto(
                        1L,
                        idInexistente,
                        dtoAtualizacao
                )
        );

        verify(produtoRepository, times(1)).findById(idInexistente);
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve deletar um produto quando o ID existir")
    void deletarProduto_ComIdExistente_DeletaComSucesso() {
        // Arrange
        Long idExistente = 1L;

        when(produtoRepository.existsById(idExistente))
                .thenReturn(true);

        // Act
        produtoService.deletarProduto(
                1L,
                idExistente
        );

        // Assert
        verify(produtoRepository, times(1)).existsById(idExistente);
        verify(produtoRepository, times(1)).deleteById(idExistente);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar deletar produto inexistente")
    void deletarProduto_ComIdInexistente_LancaExcecao() {
        // Arrange
        Long idInexistente = 99L;

        when(produtoRepository.existsById(idInexistente))
                .thenReturn(false);

        // Act & Assert
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.deletarProduto(
                        1L,
                        idInexistente
                )
        );

        // Assert extra
        assertTrue(
                exception.getMessage().contains("Usuário não encontrado")
                        || exception.getMessage().contains("Produto")
        );

        verify(produtoRepository, times(1)).existsById(idInexistente);
        verify(produtoRepository, never()).deleteById(anyLong());
    }
}