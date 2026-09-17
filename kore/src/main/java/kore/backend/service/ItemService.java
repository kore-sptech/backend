package kore.backend.service;

import jakarta.transaction.Transactional;
import kore.backend.dto.ItemDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.mapper.ItemMapper;
import kore.backend.model.Agendamento;
import kore.backend.model.Item;
import kore.backend.model.Produto;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.repository.ItemRepository;
import kore.backend.repository.ProdutoRepository;
import kore.backend.service.validation.ItemValidationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ItemValidationService itemValidationService;

    public ItemService(
            ItemRepository itemRepository,
            ProdutoRepository produtoRepository,
            AgendamentoRepository agendamentoRepository,
            ItemValidationService itemValidationService) {
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.itemValidationService = itemValidationService;

    }

    @Transactional
    public List<Item> adicionarEstoque(ItemDTO itemDTO, Integer quantidade, Long fkProduto) {
        Produto produto = itemValidationService.buscarProdutoOuLancar(fkProduto);
        List<Item> estoque = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {

            Item item = ItemMapper.fromDto(itemDTO);
            item.setProduto(produto);
            estoque.add(item);
        }

        return itemRepository.saveAll(estoque);
    }

    public List<Item> listarEstoque(Long produtoFk) {
        List<Item> itens = itemRepository.buscarPorIdDoProduto(produtoFk)
                .orElse(Collections.emptyList());
        ;
        return itens;
    }

    @Transactional
    public void removerEstoque(Long id) {
        Item i = itemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado", id));
        i.setSeAtivo(false);
    }

    @Transactional
    public Item atualizarEstoqueComAgendamento(Long idEstoque, Long idAgendamento) {
        Item i = itemRepository.findById(idEstoque)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado", idEstoque));
        Agendamento a = itemValidationService.buscarAgendamentoOuLancar(idAgendamento);
        i.setAgendamento(a);
        i.setSeAtivo(false);
        return itemRepository.save(i);
    }

    public List<Item> buscarItensPorAgendamento(Long idSessao){
        List<Item> itens = itemRepository.buscarPorIdDoAgendamento(idSessao)
                .orElse(Collections.emptyList());
        ;
        return itens;
    }
}
