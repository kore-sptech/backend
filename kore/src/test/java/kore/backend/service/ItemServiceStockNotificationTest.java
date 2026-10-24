package kore.backend.service;

import kore.backend.messaging.NotificationMessage;
import kore.backend.messaging.NotificationOutboxService;
import kore.backend.model.Item;
import kore.backend.model.Produto;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.repository.ItemRepository;
import kore.backend.repository.ProdutoRepository;
import kore.backend.repository.TransacaoRepository;
import kore.backend.repository.UsuarioRepository;
import kore.backend.service.TransacaoService;
import kore.backend.service.validation.ItemValidationService;
import kore.backend.service.validation.TransacaoValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemServiceStockNotificationTest {

    private ItemRepository itemRepository;
    private NotificationOutboxService outboxService;
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        outboxService = mock(NotificationOutboxService.class);
        TransacaoService transacaoService = new TransacaoService(
                mock(TransacaoRepository.class),
                mock(AgendamentoRepository.class),
                mock(TransacaoValidationService.class));
        itemService = new ItemService(
                itemRepository,
                mock(ProdutoRepository.class),
                mock(AgendamentoRepository.class),
                mock(ItemValidationService.class),
                outboxService,
                transacaoService,
                mock(UsuarioRepository.class));
        ReflectionTestUtils.setField(itemService, "stockRoutingKey", "notification.stock.below-threshold");
    }

    @Test
    void publishesOnlyWhenStockCrossesIntoAtOrBelowThreshold() {
        Produto product = product(5);
        Item item = item(product);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.contarItensAtivosPorProduto(3L)).thenReturn(6L, 5L);
        when(itemRepository.save(item)).thenReturn(item);

        itemService.removerEstoque(10L);

        verify(outboxService).enqueue(any(NotificationMessage.class), eq("notification.stock.below-threshold"));
    }

    @Test
    void doesNotPublishAgainWhenStockWasAlreadyAtOrBelowThreshold() {
        Produto product = product(5);
        Item item = item(product);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.contarItensAtivosPorProduto(3L)).thenReturn(5L, 4L);
        when(itemRepository.save(item)).thenReturn(item);

        itemService.removerEstoque(10L);

        verify(outboxService, never()).enqueue(any(NotificationMessage.class), any());
    }

    private static Produto product(int threshold) {
        Produto product = new Produto();
        product.setId(3L);
        product.setNome("Shampoo");
        product.setFkUsuario(8L);
        product.setQtdMinAlerta(threshold);
        return product;
    }

    private static Item item(Produto product) {
        Item item = new Item();
        item.setId(10L);
        item.setSeAtivo(true);
        item.setProduto(product);
        return item;
    }
}
