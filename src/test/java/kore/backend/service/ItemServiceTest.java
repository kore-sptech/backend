package kore.backend.service;

import kore.backend.dto.ItemDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Item;
import kore.backend.model.Produto;
import kore.backend.repository.ItemRepository;
import kore.backend.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    @DisplayName("Deve adicionar itens ao estoque quando o produto existir")
    void adicionarEstoque_ComProdutoExistente_RetornaListaDeItensSalvos() {
        // Arrange
        Long fkProduto = 1L;
        Integer quantidade = 3;
        ItemDTO dto = new ItemDTO(LocalDateTime.now(), true, 50.0f, LocalDateTime.now().plusMonths(6));

        Produto produtoMock = new Produto();
        produtoMock.setId(fkProduto);

        when(produtoRepository.findById(fkProduto)).thenReturn(Optional.of(produtoMock));

        // Simula o saveAll retornando a mesma lista que recebeu
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<Item> resultado = itemService.adicionarEstoque(dto, quantidade, fkProduto);

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals(produtoMock, resultado.get(0).getProduto()); // Verifica se vinculou o produto
        verify(produtoRepository, times(1)).findById(fkProduto);
        verify(itemRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao tentar adicionar estoque para produto inexistente")
    void adicionarEstoque_ComProdutoInexistente_LancaExcecao() {
        // Arrange
        Long fkProduto = 99L;
        Integer quantidade = 2;
        ItemDTO dto = new ItemDTO(LocalDateTime.now(), true, 50.0f, null);

        when(produtoRepository.findById(fkProduto)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> itemService.adicionarEstoque(dto, quantidade, fkProduto));

        assertEquals("Produto não encontrado com ID: " + fkProduto, exception.getMessage());
        verify(produtoRepository, times(1)).findById(fkProduto);
        verify(itemRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve retornar lista de itens quando buscar por ID do produto existente")
    void listarEstoque_ComItensEncontrados_RetornaLista() {
        // Arrange
        Long fkProduto = 1L;
        Item item1 = new Item();
        Item item2 = new Item();
        List<Item> listaMock = List.of(item1, item2);

        when(itemRepository.buscarPorIdDoProduto(fkProduto)).thenReturn(Optional.of(listaMock));

        // Act
        List<Item> resultado = itemService.listarEstoque(fkProduto);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(itemRepository, times(1)).buscarPorIdDoProduto(fkProduto);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o Optional de itens vier vazio")
    void listarEstoque_ComNenhumItemEncontrado_RetornaListaVazia() {
        // Arrange
        Long fkProduto = 1L;
        when(itemRepository.buscarPorIdDoProduto(fkProduto)).thenReturn(Optional.empty());

        // Act
        List<Item> resultado = itemService.listarEstoque(fkProduto);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(itemRepository, times(1)).buscarPorIdDoProduto(fkProduto);
    }

    @Test
    @DisplayName("Deve deletar um item do estoque quando o ID existir")
    void removerEstoque_ComIdExistente_DeletaComSucesso() {
        // Arrange
        Long idExistente = 1L;
        when(itemRepository.existsById(idExistente)).thenReturn(true);

        // Act
        itemService.removerEstoque(idExistente);

        // Assert
        verify(itemRepository, times(1)).existsById(idExistente);
        verify(itemRepository, times(1)).deleteById(idExistente);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar deletar item inexistente")
    void removerEstoque_ComIdInexistente_LancaExcecao() {
        // Arrange
        Long idInexistente = 99L;
        when(itemRepository.existsById(idInexistente)).thenReturn(false);

        // Act & Assert
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> itemService.removerEstoque(idInexistente));

        assertEquals("Item não encontrado", exception.getMessage());
        verify(itemRepository, times(1)).existsById(idInexistente);
        verify(itemRepository, never()).deleteById(anyLong());
    }
}