# SDD: Integração do micro-serviço de notificações com a API da Kore

## Identificação:

- Versão 1.0.0
- Responsável: Vitor Souza Librelon Restini
- Card: https://trello.com/c/iSl3UsX5
- Data: 06/10/2026

## Objetivo

Realizar a integração do domínio de notificação com os domínios de produto/estoque e agendamento. Essa integração é orientada pelos princípios da arquitetura orientada a eventos, sendo o RabbitMQ o serviço de mensageria.

## Critérios de aceite

- Consumidor do RabbitMQ configurado para capturar eventos de agendamento próximo (janela de 15 minutos) e disparar o fluxo de notificação
- API publicando e consumidor do RabbitMQ processando eventos quando a quantidade de itens ativos do produto ficar menor ou igual a `Produto.qtdMinAlerta`, na transição para essa condição
- Lógica de salvamento da notificação gerada implementada com sucesso no banco de dados utilizando a modelagem existente
- Tratamento de erros e reprocessamento (Dead Letter Exchange/Queue, se aplicável) configurados para as filas de eventos

## Plano de implementação

### Estado atual e escopo

- A API principal (`kore`) já verifica agendamentos periodicamente em `AgendamentoScheduler` e publica um evento interno do Spring. `NotificacaoObserver` persiste uma notificação no modelo `kore.backend.model.Notificacao` e envia o evento por SSE. Esse fluxo não publica no RabbitMQ.
- O microsserviço `notification` já tem `NotificationConsumer`, configuração RabbitMQ, `EmitNotificationUseCase` e repositório para persistir no modelo `kore.notification.entities.Notification`. Atualmente o consumidor apenas registra o conteúdo recebido no log; a fila e o binding configurados não representam os tipos de evento de negócio.
- A API já armazena `Produto.qtdMinAlerta`, que é o limiar de estoque. O estoque deve ser calculado pelos itens ativos do produto; confirmar que essa é a semântica de negócio antes de publicar alertas.
- Há dois modelos/tabelas de notificação independentes. Para os eventos desta integração, usar como destino de persistência o modelo e o repositório já pertencentes ao microsserviço `notification`; não criar uma terceira modelagem. Antes de retirar qualquer persistência/SSE de `NotificacaoObserver`, preservar ou substituir explicitamente o comportamento existente da API.
- Alterações ficam limitadas ao fluxo API → RabbitMQ → microsserviço de notificações e aos testes/documentação diretamente relacionados. Não alterar o contrato SSE existente, salvo se necessário e com cobertura de regressão.

### Fluxos de negócio

#### Agendamento próximo

1. Ajustar o agendador da API para consultar agendamentos pendentes com início dentro da janela inclusiva `[agora, agora + 15 minutos]`. A consulta deve ocorrer em intervalo configurado e menor que a janela, sem depender de um evento interno do Spring como mecanismo de integração.
2. Para cada agendamento elegível, publicar um evento persistente no RabbitMQ com identificador único, versão do contrato, tipo do evento e dados necessários para criar a notificação. Incluir pelo menos o identificador do agendamento, nome do cliente e horário de início; não enviar entidades JPA.
3. No microsserviço, validar o evento, convertê-lo para os campos aceitos por `EmitNotificationUseCase` e persistir uma notificação do tipo de agendamento com título, mensagem e nível apropriados.
4. Tornar o processamento idempotente. O agendador pode encontrar o mesmo agendamento em execuções consecutivas e o broker pode reenviar mensagens. Registrar um identificador de evento idempotente no modelo persistido (com restrição única) ou adotar mecanismo equivalente que sobreviva a reinícios.

#### Estoque abaixo do limite

1. Na API, verificar estoque após operações que alterem a quantidade disponível, incluindo entrada, baixa/consumo e desativação de itens. Centralizar a regra em serviço reutilizável, em vez de duplicá-la nos controllers.
2. Calcular a quantidade disponível usando itens ativos e comparar com `Produto.qtdMinAlerta`. Publicar o evento apenas quando houver estoque menor ou igual ao limite e a condição passar de não-alerta para alerta; isso evita emitir a mesma notificação a cada operação enquanto o estoque continuar baixo.
3. O evento deve conter identificador único, versão, identificadores do produto/usuário, nome do produto, quantidade disponível e limiar configurado. O consumidor persiste uma notificação de estoque usando o tipo e nível compatíveis com os enums do microsserviço.
4. Definir o comportamento quando `qtdMinAlerta` for nulo ou não configurado (recomendação: não publicar alerta e registrar a condição conforme o padrão de log do projeto). Garantir também que a verificação use a mesma transação/visão atualizada das alterações de estoque.

### Contrato e topologia RabbitMQ

- Definir DTOs de evento versionados e serializados em JSON. Usar tipos explícitos, por exemplo `APPOINTMENT_UPCOMING` e `STOCK_BELOW_THRESHOLD`, em vez de compartilhar classes de domínio entre os serviços.
- Configurar uma exchange `topic` durável, uma fila durável de consumo do microsserviço e routing keys distintas para cada tipo de evento. Remover ou substituir o binding de exemplo `foo.bar.#`; API e microsserviço devem usar exatamente os mesmos nomes via propriedades de configuração.
- Configurar fila de dead letter e sua exchange/binding. Usar mensagens persistentes, retries limitados para falhas transitórias e encaminhar mensagens inválidas ou esgotadas para a DLQ sem requeue infinito.
- Confirmar a publicação (publisher confirm/retorno de mensagem não roteada) e registrar falhas de publicação de modo visível. Não considerar o evento publicado quando o broker rejeitar ou não confirmar a mensagem.
- O consumidor só deve confirmar a mensagem depois de a persistência/idempotência concluir. Erro transitório de banco deve acionar retry; erro permanente de contrato deve ir para DLQ com contexto suficiente para diagnóstico, sem incluir segredos.
- Externalizar exchange, filas, routing keys, tentativas e intervalo de retry em propriedades Spring coerentes nos dois serviços, com valores padrão de desenvolvimento documentados. Não colocar credenciais no código.

### Persistência e compatibilidade

- Reutilizar o caso de uso `EmitNotificationUseCase` e o repositório existente no microsserviço, ampliando seu contrato apenas para os campos de negócio necessários.
- Corrigir/compatibilizar os valores de tipo de notificação entre produtor e consumidor. O enum do microsserviço contém atualmente `APPOIMENTMENT` (grafia divergente) e `STOCK`; definir um valor de agendamento canônico e tratar compatibilidade antes de renomear dados existentes.
- Adicionar ao modelo persistido o dado mínimo necessário para idempotência, com migração compatível com notificações já existentes e índice/constraint única. Verificar nomes reais das tabelas e configuração de schema em ambos os serviços antes de aplicar a alteração.
- Manter os campos atuais de título, mensagem, tipo, nível, lida e data de criação. Não persistir a mesma notificação em dois repositórios sem uma decisão explícita sobre qual deles é a fonte de verdade.

### Etapas sugeridas

1. **Fechar contratos e regras:** definir DTOs/versionamento, routing keys, janela exata de 15 minutos, semântica de estoque ativo, comportamento do limiar ausente, níveis/tipos e fonte de verdade da persistência. Registrar a decisão sobre como manter o fluxo SSE atual.
2. **Preparar mensageria:** alinhar propriedades dos dois serviços e declarar exchange, filas de eventos, bindings, DLX/DLQ e política de retry; verificar a inicialização no Docker Compose de desenvolvimento e produção.
3. **Publicar eventos na API:** adaptar a publicação de agendamentos para RabbitMQ e adicionar publicação de estoque nos pontos de alteração de quantidade, com consultas/regras centralizadas e idempotência no produtor quando aplicável.
4. **Processar e persistir no microsserviço:** implementar DTOs/validação, tratamento por tipo de evento, conversão para o caso de uso existente, persistência e deduplicação; remover o log-only do consumidor.
5. **Integrar e preservar comportamento:** exercitar os dois fluxos com RabbitMQ e banco reais em ambiente de teste; validar que SSE e demais consumidores atuais não recebem notificações duplicadas nem perdem comportamento.
6. **Documentar operação:** atualizar documentação de configuração com nomes de exchange/filas, routing keys, políticas de retry/DLQ e procedimento de inspeção/reprocessamento manual seguro.

### Testes e validação

- **API — agendamento:** testar limites da janela (imediatamente dentro, exatamente em 15 minutos e fora dela), status elegível/não elegível, publicação do evento esperado e ausência de publicação duplicada em ciclos subsequentes.
- **API — estoque:** testar acima, igual e abaixo de `qtdMinAlerta`; mudança de condição para baixo; estoque já abaixo sem nova transição; entrada/baixa/desativação de item; limiar nulo; e cálculo apenas de itens ativos.
- **Microsserviço — consumidor:** testar cada tipo de evento, validação de payload inválido, mapeamento de tipo/nível/mensagem, persistência uma única vez mesmo com reentrega e comportamento para falha de banco.
- **RabbitMQ:** testar roteamento para cada fila, confirmação de publicação, retry limitado e encaminhamento para DLQ após falha permanente/esgotamento de tentativas.
- **Regressão:** executar os testes Maven dos módulos `kore` e `notification`; em teste de integração, confirmar a linha persistida no schema/tabela selecionado, a deduplicação e a preservação do fluxo SSE existente.
- **Aceite manual:** subir a stack de desenvolvimento, gerar um agendamento elegível e um produto cujo estoque cruza o limite; confirmar mensagens nas filas corretas, notificações persistidas e, em cenário forçado de erro, mensagem disponível na DLQ.

### Decisões alinhadas e resultado

- As notificações recebidas pelo RabbitMQ são persistidas no modelo/tabela do microsserviço (`notifications`); a persistência e o SSE já existentes na API permanecem independentes e inalterados.
- O alerta de estoque é emitido na transição de quantidade acima do limite para quantidade menor ou igual ao limite. A quantidade considera itens ativos.
- O microsserviço não passa a publicar SSE nesta entrega.
- A API utiliza um outbox transacional com confirmação do broker. O consumidor usa `source_event_id` único para deduplicar reentregas; agendamentos têm identificador determinístico por agendamento e cada nova transição de estoque recebe identificador próprio.
- A DLQ é configurada com três tentativas de consumo antes de rejeitar a mensagem para a fila morta. Reprocessamento manual parte da interface de gerenciamento do RabbitMQ após corrigir a causa da falha.
