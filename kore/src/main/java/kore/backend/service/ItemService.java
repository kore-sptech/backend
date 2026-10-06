package kore.backend.service;

import jakarta.transaction.Transactional;
import kore.backend.dto.ItemDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.messaging.NotificationMessage;
import kore.backend.messaging.NotificationOutboxService;
import kore.backend.mapper.ItemMapper;
import kore.backend.model.Agendamento;
import kore.backend.model.Item;
import kore.backend.model.Produto;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.repository.ItemRepository;
import kore.backend.repository.ProdutoRepository;
import kore.backend.service.validation.ItemValidationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ItemValidationService itemValidationService;
    private final NotificationOutboxService notificationOutboxService;

    @Value("${app.rabbitmq.routing-key.stock}")
    private String stockRoutingKey;

    public ItemService(
            ItemRepository itemRepository,
            ProdutoRepository produtoRepository,
            AgendamentoRepository agendamentoRepository,
            ItemValidationService itemValidationService,
            NotificationOutboxService notificationOutboxService) {
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.itemValidationService = itemValidationService;
        this.notificationOutboxService = notificationOutboxService;

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
        Produto produto = i.getProduto();
        long quantidadeAnterior = itemRepository.contarItensAtivosPorProduto(produto.getId());
        i.setSeAtivo(false);
        itemRepository.save(i);
        verificarTransicaoEstoque(produto, quantidadeAnterior);
    }

    @Transactional
    public Item atualizarEstoqueComAgendamento(Long idEstoque, Long idAgendamento) {
        Item i = itemRepository.findById(idEstoque)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado", idEstoque));
        Produto produto = i.getProduto();
        long quantidadeAnterior = itemRepository.contarItensAtivosPorProduto(produto.getId());
        Agendamento a = itemValidationService.buscarAgendamentoOuLancar(idAgendamento);
        i.setAgendamento(a);
        i.setSeAtivo(false);
        Item itemAtualizado = itemRepository.save(i);
        verificarTransicaoEstoque(produto, quantidadeAnterior);
        return itemAtualizado;
    }

    private void verificarTransicaoEstoque(Produto produto, long quantidadeAnterior) {
        Integer limite = produto.getQtdMinAlerta();
        if (limite == null) {
            return;
        }

        long quantidadeAtual = itemRepository.contarItensAtivosPorProduto(produto.getId());
        if (quantidadeAnterior <= limite || quantidadeAtual > limite) {
            return;
        }

        notificationOutboxService.enqueue(new NotificationMessage(
                "stock-below-threshold:" + UUID.randomUUID(),
                1,
                "STOCK_BELOW_THRESHOLD",
                "Estoque baixo",
                String.format("O produto %s atingiu %d itens disponíveis (limite: %d).",
                        produto.getNome(), quantidadeAtual, limite),
                "STOCK",
                "WARNING",
                null,
                null,
                null,
                produto.getId(),
                produto.getFkUsuario(),
                produto.getNome(),
                Math.toIntExact(quantidadeAtual),
                limite),
                stockRoutingKey);
    }

    public List<Item> buscarItensPorAgendamento(Long idSessao){
        List<Item> itens = itemRepository.buscarPorIdDoAgendamento(idSessao)
                .orElse(Collections.emptyList());
        ;
        return itens;
    }
}
