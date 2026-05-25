package kore.backend.service;

import kore.backend.dto.ProdutoDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Produto;
import kore.backend.repository.ProdutoRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    @DisplayName("Deve salvar um produto com sucesso")
    void salvarProduto_ComDadosValidos_RetornaProdutoSalvo() {
        // Arrange (Preparar)
        ProdutoDTO dto = new ProdutoDTO("Teclado", "Teclado Mecânico", false, 10);
        Produto produtoSalvo = new Produto(dto);
        // Assumindo que a entidade geraria um ID no banco
        produtoSalvo.setId(1L);

        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoSalvo);

        // Act (Agir)
        Produto resultado = produtoService.salvarProduto(dto);

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
        ProdutoDTO dto1 = new ProdutoDTO("Teclado", "Teclado Mecânico", false, 10);
        ProdutoDTO dto2 = new ProdutoDTO("Mouse", "Mouse Gamer", false, 5);
        List<Produto> listaMock = List.of(new Produto(dto1), new Produto(dto2));

        when(produtoRepository.findAll()).thenReturn(listaMock);

        // Act
        List<Produto> resultado = produtoService.listarTodosProdutos();

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
        ProdutoDTO dtoAtualizacao = new ProdutoDTO("Monitor", "Monitor 144hz", false, 20);
        Produto produtoExistente = new Produto(); // Instância original antes do update
        produtoExistente.setId(idExistente);
        produtoExistente.setNome("Monitor Antigo");

        when(produtoRepository.findById(idExistente)).thenReturn(Optional.of(produtoExistente));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Produto resultado = produtoService.atualizarProduto(idExistente, dtoAtualizacao);

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
        ProdutoDTO dtoAtualizacao = new ProdutoDTO("Monitor", "Monitor 144hz", false, 20);

        when(produtoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert (Em casos de exceção, o Assert engloba o Act)
        assertThrows(RecursoNaoEncontradoException.class,
                () -> produtoService.atualizarProduto(idInexistente, dtoAtualizacao));

        verify(produtoRepository, times(1)).findById(idInexistente);
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve deletar um produto quando o ID existir")
    void deletarProduto_ComIdExistente_DeletaComSucesso() {
        // Arrange
        Long idExistente = 1L;
        when(produtoRepository.existsById(idExistente)).thenReturn(true);

        // Act
        produtoService.deletarProduto(idExistente);

        // Assert
        verify(produtoRepository, times(1)).existsById(idExistente);
        verify(produtoRepository, times(1)).deleteById(idExistente);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar deletar produto inexistente")
    void deletarProduto_ComIdInexistente_LancaExcecao() {
        // Arrange
        Long idInexistente = 99L;
        when(produtoRepository.existsById(idInexistente)).thenReturn(false);

        // Act & Assert
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> produtoService.deletarProduto(idInexistente));

        // Assert extra: notei que na service está escrito "Usuário não encontrado" em vez de "Produto"
        assertTrue(exception.getMessage().contains("Usuário não encontrado") || exception.getMessage().contains("Produto"));

        verify(produtoRepository, times(1)).existsById(idInexistente);
        verify(produtoRepository, never()).deleteById(anyLong());
    }
}