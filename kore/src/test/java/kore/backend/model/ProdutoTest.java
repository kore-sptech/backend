package kore.backend.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ProdutoTest {

    @Test
    void adicionarEstoque_deveAdicionarItensNaListaInterna() {
        Produto produto = new Produto();
        Item itemExistente = new Item();
        Item novoItem = new Item();
        produto.setItens(new ArrayList<>(List.of(itemExistente)));

        produto.adicionarEstoque(List.of(novoItem));

        assertEquals(2, produto.getItens().size());
        assertSame(novoItem, produto.getItens().get(1));
    }

    @Test
    void adicionarEstoque_comListaNula_naoDeveAlterarItens() {
        Produto produto = new Produto();

        produto.adicionarEstoque(null);

        assertEquals(0, produto.getItens().size());
    }
}
