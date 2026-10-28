package kore.backend.events;

import kore.backend.messaging.NotificationMessage;
import kore.backend.messaging.NotificationOutboxService;
import kore.backend.model.Item;
import kore.backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EstoqueValidadeScheduler {

    private final ItemRepository itemRepository;
    private final NotificationOutboxService notificationOutboxService;

    @Value("${app.rabbitmq.routing-key.stock}")
    private String stockRoutingKey;

    // Executa diariamente às 08:00
    @Scheduled(cron = "0 0 8 * * *")
    public void verificarValidadeEstoque() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite15Dias = agora.plusDays(15);

        // 1. Verificar itens próximos da validade (próximos 15 dias), agrupados por produto
        List<Item> itensProximos = itemRepository.findByDataValidadeBetweenAndSeAtivo(agora, limite15Dias, true);

        Map<Long, List<Item>> itensPorProduto = itensProximos.stream()
                .collect(Collectors.groupingBy(item -> item.getProduto().getId()));

        itensPorProduto.forEach((produtoId, itens) -> {
            if (itens.isEmpty()) return;
            Item primeiro = itens.get(0);
            String nomeProduto = primeiro.getProduto().getNome();
            long diasRestantes = ChronoUnit.DAYS.between(agora, primeiro.getDataValidade());

            String eventId = String.format("item-expiring-prod:%d:%s", produtoId, primeiro.getDataValidade().toLocalDate());

            NotificationMessage msg = new NotificationMessage(
                    eventId,
                    1,
                    "ITEM_EXPIRING_SOON",
                    "Validade Próxima: " + nomeProduto,
                    String.format("O lote #K%d de %s vence em %d dias. Priorize o uso ou remova do estoque principal.",
                            produtoId, nomeProduto, Math.max(0, diasRestantes)),
                    "STOCK",
                    "WARNING",
                    null,
                    null,
                    null,
                    produtoId,
                    primeiro.getProduto().getFkUsuario(),
                    nomeProduto,
                    itens.size(),
                    null,
                    "ATENCAO",
                    "STOCK",
                    produtoId,
                    "VER_ITEM"
            );

            notificationOutboxService.enqueue(msg, stockRoutingKey);
            log.info("[NOTIF-EMIT-KORE] Notificação de validade próxima emitida | EventId: {} | Produto ID: {} | Produto: {} | Dias Restantes: {}",
                    eventId, produtoId, nomeProduto, diasRestantes);
        });

        // 2. Verificar itens já vencidos e ainda ativos (risco sanitário)
        List<Item> itensVencidos = itemRepository.findByDataValidadeBeforeAndSeAtivo(agora, true);
        Map<Long, List<Item>> vencidosPorProduto = itensVencidos.stream()
                .collect(Collectors.groupingBy(item -> item.getProduto().getId()));

        vencidosPorProduto.forEach((produtoId, itens) -> {
            if (itens.isEmpty()) return;
            Item primeiro = itens.get(0);
            String nomeProduto = primeiro.getProduto().getNome();

            String eventId = String.format("item-expired-prod:%d:%s", produtoId, agora.toLocalDate());

            NotificationMessage msg = new NotificationMessage(
                    eventId,
                    1,
                    "ITEM_EXPIRED_ACTIVE",
                    "Estoque Crítico: " + nomeProduto,
                    String.format("O estoque de %s atingiu o nível mínimo. Restam apenas %d unidades.",
                            nomeProduto, itens.size()),
                    "STOCK",
                    "ERROR",
                    null,
                    null,
                    null,
                    produtoId,
                    primeiro.getProduto().getFkUsuario(),
                    nomeProduto,
                    itens.size(),
                    null,
                    "CRITICO",
                    "STOCK",
                    produtoId,
                    "REGISTRAR_PRODUTO"
            );

            notificationOutboxService.enqueue(msg, stockRoutingKey);
            log.info("[NOTIF-EMIT-KORE] Notificação de item vencido ativa emitida | EventId: {} | Produto ID: {} | Produto: {} | Quantidade Vencida: {}",
                    eventId, produtoId, nomeProduto, itens.size());
        });
    }
}
