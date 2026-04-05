package kore.backend.service;

import jakarta.transaction.Transactional;
import kore.backend.dto.ItemDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.model.Item;
import kore.backend.model.Produto;
import kore.backend.repository.ItemRepository;
import kore.backend.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ProdutoRepository produtoRepository;

    public ItemService(ItemRepository itemRepository, ProdutoRepository produtoRepository) {
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public List<Item> adicionarEstoque(ItemDTO itemDTO, Integer quantidade, Long fkProduto){
        Produto produto = produtoRepository.findById(fkProduto)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + fkProduto));
        List<Item> estoque = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Item item = new Item(itemDTO);
            item.setProduto(produto);
            estoque.add(item);
        }

        return itemRepository.saveAll(estoque);
    }
    public List<Item> listarEstoque(Long produtoFk){
        List<Item> itens = itemRepository.buscarPorIdDoProduto(produtoFk)
                        .orElse(Collections.emptyList());;
        return itens;
    }
    @Transactional
    public void removerEstoque(Long id){
        if (!itemRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Item não encontrado", id);
        }
        itemRepository.deleteById(id);
    }
    

}
