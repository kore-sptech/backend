package kore.backend.service;

import jakarta.transaction.Transactional;
import kore.backend.dto.ItemDTO;
import kore.backend.dto.ItemEstoqueResponseDTO;
import kore.backend.exception.RecursoNaoEncontradoException;
import kore.backend.messaging.NotificationMessage;
import kore.backend.messaging.NotificationOutboxService;
import kore.backend.mapper.ItemMapper;
import kore.backend.model.Agendamento;
import kore.backend.model.Item;
import kore.backend.model.Usuario;
import kore.backend.repository.UsuarioRepository;
import kore.backend.service.TransacaoService;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.repository.ItemRepository;
import kore.backend.model.Produto;
import kore.backend.repository.ProdutoRepository;
import kore.backend.service.validation.ItemValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ItemValidationService itemValidationService;
    private final NotificationOutboxService notificationOutboxService;
    private final TransacaoService transacaoService;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.rabbitmq.routing-key.stock}")
    private String stockRoutingKey;

    public ItemService(
            ItemRepository itemRepository,
            ProdutoRepository produtoRepository,
            AgendamentoRepository agendamentoRepository,
            ItemValidationService itemValidationService,
            NotificationOutboxService notificationOutboxService,
            TransacaoService transacaoService,
            UsuarioRepository usuarioRepository) {
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.itemValidationService = itemValidationService;
        this.notificationOutboxService = notificationOutboxService;
        this.transacaoService = transacaoService;
        this.usuarioRepository = usuarioRepository;

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

        List<Item> salvos = itemRepository.saveAll(estoque);

        if (Boolean.TRUE.equals(itemDTO.novoItem()) && itemDTO.valorUnitario() != null) {
            double valorTotal = quantidade * itemDTO.valorUnitario();
            Usuario usuario = usuarioRepository.findById(produto.getFkUsuario())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado", produto.getFkUsuario()));
            transacaoService.criarTransacaoDeEstoque(
                    valorTotal,
                    "Compra de estoque — " + produto.getNome(),
                    usuario,
                    produto);
        }

        return salvos;
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
        Integer limite = produto.getQtdMinAlerta() != null ? produto.getQtdMinAlerta() : 5;

        long quantidadeAtual = itemRepository.contarItensAtivosPorProduto(produto.getId());
        if (quantidadeAnterior <= limite || quantidadeAtual > limite) {
            return;
        }

        NotificationMessage msg = new NotificationMessage(
                "stock-below-threshold:" + UUID.randomUUID(),
                1,
                "STOCK_BELOW_THRESHOLD",
                "Estoque crítico: " + produto.getNome(),
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
                limite,
                "CRITICO",
                "STOCK",
                produto.getId(),
                "REGISTRAR_PRODUTO");
        notificationOutboxService.enqueue(msg, stockRoutingKey);
        log.info("[NOTIF-EMIT-KORE] Notificação de estoque abaixo do limite emitida | EventId: {} | Produto ID: {} | Produto: {} | Qtd Atual: {} | Limite: {}",
                msg.eventId(), produto.getId(), produto.getNome(), quantidadeAtual, limite);
    }

    @Transactional
    public Item desassociarEstoqueDeAgendamento(Long idEstoque) {
        Item i = itemRepository.findById(idEstoque)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado", idEstoque));
        Produto produto = i.getProduto();
        long quantidadeAnterior = itemRepository.contarItensAtivosPorProduto(produto.getId());
        i.setAgendamento(null);
        i.setSeAtivo(true);
        Item itemAtualizado = itemRepository.save(i);
        verificarTransicaoEstoque(produto, quantidadeAnterior);
        return itemAtualizado;
    }

    public List<ItemEstoqueResponseDTO> buscarItensPorAgendamento(Long idAgendamento){
        List<Item> itens = itemRepository.buscarPorIdDoAgendamento(idAgendamento);
        return itens.stream()
                .map(ItemEstoqueResponseDTO::fromEntity)
                .toList();
    }
}
