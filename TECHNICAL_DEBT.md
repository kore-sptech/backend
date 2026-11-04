# 🔍 Relatório de Débitos Técnicos — Projeto Kore SPTech

> **Analisado em:** 04/11/2026 · **Escopo:** `backend/kore`, `backend/notification`, `front-end`, `infra (docker/nginx)`

---

## Sumário Executivo

| Categoria | Severidade | Qtd. Issues |
|---|---|---|
| Arquitetura | 🟡 Média–Alta | 7 |
| Vulnerabilidades de Segurança | 🔴 Alta | 8 |
| Redundância de Código | 🟡 Média | 6 |
| Estado de Dependências | 🟡 Média | 5 |
| Qualidade de Infraestrutura | 🟡 Média | 7 |
| Bugs / Comportamentos Errados | 🔴 Alta | 6 |

---

## 1. 🏛️ Arquitetura

### 1.1 — Dois microsserviços compartilham o mesmo banco de dados (MySQL)

**Repo:** `backend/kore` · `backend/notification`
**Severidade:** 🔴 Alta

Ambos os microsserviços (`kore` e `notification`) apontam para o **mesmo schema** `test` no mesmo host MySQL:

```properties
# kore/application.properties
spring.datasource.url=jdbc:mysql://database:3306/test

# notification/application.properties
spring.datasource.url=jdbc:mysql://database:3306/test
```

Isso cria acoplamento de dados entre os serviços — qualquer migração de schema quebrará ambos simultaneamente. Em uma arquitetura de microsserviços, cada serviço deve possuir seu próprio banco (database-per-service pattern).

**Recomendação:** Criar schemas separados (`kore_db` e `notification_db`) ou usar bancos distintos via Docker Compose.

---

### 1.2 — Microsserviço de Notification expõe porta pública desnecessariamente

**Repo:** `backend/docker-compose.yml`
**Severidade:** 🟡 Média

```yaml
kore-notification:
  ports:
    - "8081:8081"
```

O serviço de notificação só recebe mensagens via RabbitMQ; não precisa ter sua porta exposta externamente. Expor essa porta aumenta a superfície de ataque.

**Recomendação:** Remover o mapeamento de portas do `kore-notification`. A comunicação deve ser apenas interna à rede Docker.

---

### 1.3 — `NotificacaoController` acessa o repositório diretamente, violando a separação de camadas

**Repo:** `backend/kore`
**Arquivo:** [`NotificacaoController.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/controller/NotificacaoController.java#L26-L30)
**Severidade:** 🟡 Média

```java
public class NotificacaoController {
    private final NotificacaoRepository notificacaoRepository; // ← repositório direto no controller
```

O controller chama `notificacaoRepository.findAll()` e aplica lógica de negócio (filtragem por severidade) diretamente na camada de apresentação, sem passar por uma `Service`. Isso fere o padrão Controller → Service → Repository.

**Recomendação:** Criar `NotificacaoService` com os métodos `listar()`, `marcarComoLida()`, `resumo()`.

---

### 1.4 — SSE sem autenticação e sem multi-tenant

**Repo:** `backend/kore`
**Arquivo:** [`SecurityConfiguration.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/config/security/SecurityConfiguration.java#L44)
**Severidade:** 🔴 Alta

```java
private final String[] ROUTE_PERMIT = {
    "/sse/stream",   // ← endpoint SSE liberado sem autenticação
    ...
};
```

O endpoint SSE está em `ROUTE_PERMIT` (público), e o `ServerSentEventService` mantém um único `List<SseEmitter>` global — ou seja, qualquer cliente conectado recebe **notificações de todos os usuários**. Não há isolamento por `usuarioId`.

**Recomendação:** Autenticar o SSE via token JWT no `EventSource` e manter um `Map<Long, List<SseEmitter>>` por usuário.

---

### 1.5 — `DatabaseSeeder` sempre executa em produção

**Repo:** `backend/kore`
**Arquivo:** [`DatabaseSeeder.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/seeder/DatabaseSeeder.java#L25)
**Severidade:** 🟡 Média

O seeder implementa `CommandLineRunner` sem nenhuma proteção de perfil (`@Profile("dev")` ou verificação de variável de ambiente). Ele executa a cada inicialização em qualquer ambiente — apenas ignora se `produtoRepository.count() > 0`. Isso é frágil em ambientes de CI ou se a tabela for resetada em produção.

**Recomendação:** Proteger com `@Profile("dev")` ou `@ConditionalOnProperty(name="app.seed.enabled")`.

---

### 1.6 — Dois arquivos `nginx.conf` distintos com configurações divergentes

**Repo:** Raiz + `backend/`
**Arquivos:** [`/nginx/nginx.conf`](file:///home/henry/Projetos/Faculdade/kore-sptech/nginx/nginx.conf) e [`/backend/nginx/nginx.conf`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/nginx/nginx.conf)
**Severidade:** 🟡 Média

O arquivo na raiz tem 3 instâncias de frontend (`frontend-1`, `frontend-2`, `frontend-3`) enquanto o do `backend/` tem apenas 2. O `docker-compose.yml` da raiz declara 3 containers mas o `nginx.conf` do backend é o que é montado pelo compose principal — criando desalinhamento entre os configs.

**Recomendação:** Manter um único `nginx.conf` como fonte de verdade.

---

### 1.7 — `Produto.adicionarEstoque()` tem bug de lógica que nunca deve ser chamado

**Repo:** `backend/kore`
**Arquivo:** [`Produto.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/model/Produto.java#L77-L79)
**Severidade:** 🟡 Média

```java
public void adicionarEstoque(List<Item> itens) {
    itens.forEach(item -> itens.add(item)); // ← ConcurrentModificationException garantida
}
```

Esse método itera e adiciona ao **mesmo** `List<Item>` recebido como parâmetro — o que sempre lança `ConcurrentModificationException`. O método não é chamado pelo código atual, mas sua presença indica código morto com bug crítico.

---

## 2. 🔐 Vulnerabilidades de Segurança

### 2.1 — Senha do banco de dados vazia em produção

**Repo:** `backend/docker-compose.yml` + `application.properties`
**Severidade:** 🔴 Crítica

```yaml
MYSQL_ROOT_PASSWORD: ""
MYSQL_ALLOW_EMPTY_PASSWORD: yes
SPRING_DATASOURCE_PASSWORD: ""
```

O banco MySQL de produção está configurado sem senha. Qualquer processo ou container na mesma rede Docker pode se conectar como root sem autenticação.

**Recomendação:** Definir `MYSQL_ROOT_PASSWORD` via secret do Docker Compose (ou variável de ambiente), nunca deixar vazio.

---

### 2.2 — JWT Secret com valor padrão hardcoded

**Repo:** `backend/kore`
**Arquivo:** [`application.properties`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/resources/application.properties#L31)
**Severidade:** 🔴 Alta

```properties
api.security.token.secret=${JWT_SECRET:my-secret-key}
```

O fallback `my-secret-key` é uma string trivial, curta e pública. Se a variável `JWT_SECRET` não for definida no ambiente (o que acontece em desenvolvimento e possivelmente em CI), qualquer pessoa pode forjar tokens JWT válidos com essa chave.

**Recomendação:** Remover o fallback e fazer a aplicação falhar na inicialização se `JWT_SECRET` não estiver definido.

---

### 2.3 — Credentials do RabbitMQ hardcoded em `notification/application.properties`

**Repo:** `backend/notification`
**Arquivo:** [`application.properties`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/notification/src/main/resources/application.properties#L15-L19)
**Severidade:** 🔴 Alta

```properties
spring.rabbitmq.host=rabbitmq          # ← hardcoded
spring.rabbitmq.username=guest         # ← hardcoded
spring.rabbitmq.password=guest         # ← hardcoded
```

As linhas 15-19 têm os valores hardcoded e as linhas 21-23 usam variáveis de ambiente — ou seja, as propriedades são **declaradas duas vezes**, e o RabbitMQ roda em produção com as credenciais padrão `guest/guest`.

**Recomendação:** Remover as linhas hardcoded e usar apenas variáveis de ambiente. Trocar as credenciais do RabbitMQ em produção.

---

### 2.4 — Endpoint `/test` público sem propósito em produção

**Repo:** `backend/kore`
**Arquivo:** [`SecurityConfiguration.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/config/security/SecurityConfiguration.java#L47-L48)
**Severidade:** 🟡 Média

```java
"/test",
"/test/**"
```

O `TestController` retorna apenas `"Test OK"`, mas a rota `/test/**` está publicamente acessível, potencialmente expondo um vetor de information disclosure se o controller for expandido inadvertidamente.

**Recomendação:** Proteger com `@Profile("dev")` ou remover em produção.

---

### 2.5 — `GET /usuarios` lista todos os usuários sem controle de acesso

**Repo:** `backend/kore`
**Arquivo:** [`UsuarioController.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/controller/UsuarioController.java#L71-L75)
**Severidade:** 🔴 Alta

```java
@GetMapping()
public ResponseEntity<List<UsuarioResponseDTO>> buscartodos() {
    List<Usuario> p = usuarioService.buscartodos();
    return ResponseEntity.ok(UsuarioMapper.toResponseList(p));
}
```

Qualquer usuário autenticado pode listar todos os cadastros do sistema. Para uma aplicação de estúdio de tatuagem individual, isso expõe dados de outros usuários desnecessariamente.

**Recomendação:** Ou remover o endpoint, ou proteger com role `ADMIN`.

---

### 2.6 — Token JWT armazenado em `localStorage` (XSS-vulnerável)

**Repo:** `front-end`
**Arquivo:** [`auth.js`](file:///home/henry/Projetos/Faculdade/kore-sptech/front-end/src/utils/auth.js#L6)
**Severidade:** 🟡 Média

```js
export function getToken() {
  return localStorage.getItem("token");
}
```

Armazenar tokens em `localStorage` os expõe a ataques XSS — qualquer script injetado pode roubar o token. A prática recomendada é usar cookies `HttpOnly` + `Secure` + `SameSite=Strict`.

**Recomendação:** Migrar o token para cookie HttpOnly gerenciado pelo backend.

---

### 2.7 — Filtro de notificações não aplica isolamento por usuário

**Repo:** `backend/kore`
**Arquivo:** [`NotificacaoController.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/controller/NotificacaoController.java#L39)
**Severidade:** 🔴 Alta

```java
List<Notificacao> notificacoes = notificacaoRepository.findAll(); // ← sem filtro por usuário
```

A entidade `Notificacao` não tem campo `fk_usuario` — logo, todas as notificações do sistema são retornadas para qualquer usuário autenticado. Em produção com múltiplos usuários, isso é uma IDOR (Insecure Direct Object Reference).

**Recomendação:** Adicionar `fk_usuario` a `Notificacao` e filtrar por `usuario.getId()`.

---

### 2.8 — `GET /test-validation` acessível publicamente em produção no front-end

**Repo:** `front-end`
**Arquivo:** [`router.jsx`](file:///home/henry/Projetos/Faculdade/kore-sptech/front-end/src/app/router.jsx#L55)
**Severidade:** 🟡 Baixa

```jsx
{ path: "/test-validation", element: <TestValidationPage /> },
```

A `TestValidationPage` está fora do `PrivateRoute`, acessível sem autenticação e permite disparar requisições autenticadas ao backend com dados inválidos. Deve ser restrita a ambientes de desenvolvimento.

**Recomendação:** Mover para dentro do `PrivateRoute` ou condicionar com `import.meta.env.DEV`.

---

## 3. 🔁 Redundância de Código

### 3.1 — Dois Dockerfiles idênticos em `kore/` e `notification/`

**Repo:** `backend/kore` · `backend/notification`
**Arquivos:** [`kore/Dockerfile`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/Dockerfile) · [`notification/Dockerfile`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/notification/Dockerfile)
**Severidade:** 🟡 Média

Ambos os arquivos são **bit-a-bit idênticos** (mesma base `maven:3.9-eclipse-temurin-21`, mesmas stages, mesmo script de entrypoint). Qualquer mudança precisa ser replicada nos dois.

**Recomendação:** Criar um único `Dockerfile.base` compartilhado usando Docker Bake ou ARG para parametrizar o módulo Maven.

---

### 3.2 — Propriedades do RabbitMQ declaradas duas vezes em `notification/application.properties`

**Repo:** `backend/notification`
**Arquivo:** [`application.properties`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/notification/src/main/resources/application.properties#L15-L23)
**Severidade:** 🟡 Baixa

```properties
spring.rabbitmq.host=rabbitmq           # linha 15 — hardcoded
spring.rabbitmq.username=guest          # linha 16
spring.rabbitmq.password=guest          # linha 17

spring.rabbitmq.host=${RABBITMQ_HOST:rabbitmq}      # linha 21 — via env var
spring.rabbitmq.username=${RABBITMQ_DEFAULT_USER:guest}  # linha 22
spring.rabbitmq.password=${RABBITMQ_DEFAULT_PASS:guest}  # linha 23
```

As mesmas propriedades são definidas duas vezes. O Spring usa o valor **mais recente** lido (linha 21-23 sobrescreve 15-17), então as linhas 15-17 são mortas e enganosas.

**Recomendação:** Remover as linhas 15-17.

---

### 3.3 — Lógica de resumo de notificações duplicada entre backend e frontend

**Repo:** `backend/kore` · `front-end`
**Arquivos:** [`NotificacaoController.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/controller/NotificacaoController.java#L84-L94) · [`notifications.js`](file:///home/henry/Projetos/Faculdade/kore-sptech/front-end/src/features/notifications/notifications.js)
**Severidade:** 🟡 Média

O backend tem um endpoint `GET /notificacoes/resumo` que faz `findAll()` e conta por severidade em memória. O frontend também chama `GET /notificacoes`, recebe a lista completa e computa seu próprio resumo em `summarizeNotifications()`. Dois lugares fazendo a mesma coisa com a mesma fonte de dados.

**Recomendação:** O backend deve retornar o resumo via uma query agregada (`GROUP BY severidade COUNT(*)`). O frontend deve apenas consumir esse endpoint.

---

### 3.4 — `SEED_NOTIFICATIONS` no frontend duplica as notificações do `DatabaseSeeder`

**Repo:** `front-end` · `backend/kore`
**Arquivos:** [`seed.js`](file:///home/henry/Projetos/Faculdade/kore-sptech/front-end/src/features/notifications/seed.js) · [`DatabaseSeeder.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/seeder/DatabaseSeeder.java)
**Severidade:** 🟡 Baixa

O frontend mantém 12 notificações fictícias hardcoded em `SEED_NOTIFICATIONS` como "placeholder enquanto não há SSE". O backend já tem um seeder que cria notificações reais no banco. As notificações do frontend são dados dummy desconectados da realidade do sistema.

**Recomendação:** Remover `SEED_NOTIFICATIONS` e usar o endpoint `GET /notificacoes` como fonte inicial (história de carregamento + loading state).

---

### 3.5 — Variáveis de paginação recalculadas em `useNotificationFeed` e `usePagination`

**Repo:** `front-end`
**Arquivos:** [`useNotificationFeed.js`](file:///home/henry/Projetos/Faculdade/kore-sptech/front-end/src/features/notifications/useNotificationFeed.js#L38-L47) · [`usePagination.js`](file:///home/henry/Projetos/Faculdade/kore-sptech/front-end/src/hooks/usePagination.js)
**Severidade:** 🟡 Baixa

O hook `useNotificationFeed` recalcula `totalPages`, `currentPage`, `firstItem`, `lastItem` manualmente, sendo que o hook genérico `usePagination` existe exatamente para isso.

**Recomendação:** Refatorar `useNotificationFeed` para delegar ao `usePagination`.

---

### 3.6 — Dois `nginx.conf` com configurações de cluster divergentes

> *(já listado em Arquitetura 1.6 — reforçado aqui como redundância de configuração)*

---

## 4. 📦 Estado de Dependências

### 4.1 — Versões diferentes do Spring Boot entre os dois microsserviços

**Repo:** `backend/kore` · `backend/notification`
**Arquivos:** [`kore/pom.xml`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/pom.xml#L8) · [`notification/pom.xml`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/notification/pom.xml#L8)
**Severidade:** 🔴 Alta

```xml
<!-- kore/pom.xml -->
<version>4.0.3</version>

<!-- notification/pom.xml -->
<version>4.1.1</version>
```

Os dois microsserviços usam versões diferentes do Spring Boot. Isso dificulta debugging, pode introduzir incompatibilidades na serialização JSON e impede upgrades sincronizados.

**Recomendação:** Alinhar ambos para a mesma versão estável mais recente (4.1.1 ou posterior).

---

### 4.2 — Versão de Testcontainers misturada (`2.0.5` para `testcontainers` e `1.21.4` para `mysql`)

**Repo:** `backend/kore`
**Arquivo:** [`kore/pom.xml`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/pom.xml#L111-L123)
**Severidade:** 🟡 Média

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>2.0.5</version>  <!-- versão 2.x -->
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <version>1.21.4</version>  <!-- versão 1.x -->
</dependency>
```

Versões da suite Testcontainers incompatíveis entre si (2.x + 1.x). Isso pode causar conflitos de classpath.

**Recomendação:** Usar `testcontainers-bom` para gerenciar todas as versões do grupo de forma consistente.

---

### 4.3 — `datafaker` em dependência de produção (deveria ser escopo `test`)

**Repo:** `backend/kore`
**Arquivo:** [`kore/pom.xml`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/pom.xml#L43-L46)
**Severidade:** 🟡 Média

```xml
<dependency>
    <groupId>net.datafaker</groupId>
    <artifactId>datafaker</artifactId>
    <version>1.9.0</version>
    <!-- sem scope → vai para produção -->
</dependency>
```

A biblioteca `datafaker` é usada exclusivamente no `DatabaseSeeder` (dados fictícios para seed). Ela está sendo empacotada no JAR de produção sem necessidade.

**Recomendação:** Mover o `DatabaseSeeder` para perfil `dev` e adicionar `<scope>test</scope>` ao `datafaker`, ou remover completamente de produção.

---

### 4.4 — `jackson-databind` com versão explícita e potencialmente divergente do BOM do Spring

**Repo:** `backend/notification`
**Arquivo:** [`notification/pom.xml`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/notification/pom.xml#L80-L83)
**Severidade:** 🟡 Baixa

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.18.2</version> <!-- ou a versão gerenciada pelo seu Spring Boot -->
</dependency>
```

O comentário no próprio arquivo indica incerteza. O Spring Boot BOM já gerencia a versão do Jackson — declarar explicitamente pode criar conflitos de versão ou mascarar atualizações de segurança futuras.

**Recomendação:** Remover a tag `<version>` e deixar o BOM do Spring Boot gerenciar.

---

### 4.5 — `springdoc-openapi` em versão potencialmente desalinhada com Spring Boot 4.x

**Repo:** `backend/kore`
**Arquivo:** [`kore/pom.xml`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/pom.xml#L48-L51)
**Severidade:** 🟡 Baixa

```xml
<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
<version>3.0.2</version>
```

A versão 3.x do SpringDoc OpenAPI é a que suporta Spring Boot 4.x, mas a versão exata `3.0.2` pode não ter suporte completo para todas as funcionalidades do Spring Boot 4.1.x. Recomenda-se verificar a matriz de compatibilidade.

---

## 5. 🐳 Qualidade de Infraestrutura

### 5.1 — MySQL rodando com usuário `root` e sem senha

**Repo:** `backend/docker-compose.yml`
**Severidade:** 🔴 Crítica

```yaml
database:
  user: "0:0"      # rodando como root do sistema operacional
  environment:
    MYSQL_ROOT_PASSWORD: ""
    MYSQL_ALLOW_EMPTY_PASSWORD: yes
```

A combinação de `user: 0:0` (root do SO) + senha vazia + `ALLOW_EMPTY_PASSWORD` representa o maior risco de segurança da infraestrutura.

**Recomendação:** Criar um usuário dedicado `kore_user` com senha forte via Docker Secret ou arquivo `.env` não versionado.

---

### 5.2 — RabbitMQ sem healthcheck de dependência no `notification`

**Repo:** `backend/docker-compose.yml`
**Severidade:** 🟡 Média

```yaml
rabbitmq:
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "status"]
```

O healthcheck existe, mas os serviços `backend` e `kore-notification` dependem via `condition: service_healthy`. Porém, `rabbitmq-diagnostics status` pode retornar `healthy` antes que os vhosts e exchanges estejam prontos — causando erros de startup nos consumidores.

**Recomendação:** Usar `rabbitmq-diagnostics check_running` ou aguardar com retry na aplicação (`spring.rabbitmq.listener.simple.retry.enabled=true` já configurado).

---

### 5.3 — Sem volume persistente para RabbitMQ

**Repo:** `backend/docker-compose.yml`
**Severidade:** 🟡 Média

O serviço `rabbitmq` não tem volume mapeado. Ao reiniciar o container, toda a fila (mensagens não processadas, configurações de exchange/queue) é perdida. Isso pode causar perda de notificações pendentes.

**Recomendação:** Adicionar volume `RABBITMQ_DATA:/var/lib/rabbitmq`.

---

### 5.4 — `docker-compose.yml` da raiz não inclui os serviços de backend

**Repo:** Raiz do projeto
**Arquivo:** [`docker-compose.yml`](file:///home/henry/Projetos/Faculdade/kore-sptech/docker-compose.yml)
**Severidade:** 🟡 Média

O `docker-compose.yml` da raiz contém apenas os 3 containers de frontend + nginx. Para subir o sistema completo, é necessário rodar também `backend/docker-compose.yml` separadamente. Não há documentação ou script que unifique os dois.

**Recomendação:** Usar `docker compose -f docker-compose.yml -f backend/docker-compose.yml up` ou criar um `docker-compose.override.yml` na raiz que inclua os serviços de backend via `extends`.

---

### 5.5 — Nginx sem HTTPS / TLS

**Repo:** Infra
**Arquivo:** [`nginx/nginx.conf`](file:///home/henry/Projetos/Faculdade/kore-sptech/nginx/nginx.conf)
**Severidade:** 🔴 Alta

A configuração do Nginx escuta apenas na porta 80 (HTTP). Em produção, todo tráfego — incluindo tokens JWT no header `Authorization` — trafega em texto puro.

**Recomendação:** Configurar TLS (certificado Let's Encrypt via Certbot ou Traefik) com redirect 301 de HTTP para HTTPS.

---

### 5.6 — Sem rate limiting no Nginx

**Repo:** Infra
**Arquivo:** [`nginx/nginx.conf`](file:///home/henry/Projetos/Faculdade/kore-sptech/nginx/nginx.conf)
**Severidade:** 🟡 Média

Não há configuração de `limit_req_zone` ou `limit_conn_zone`. O endpoint `/auth/login` é particularmente vulnerável a ataques de força bruta.

**Recomendação:** Adicionar rate limiting para `/auth/login` e `/usuarios` (cadastro).

---

### 5.7 — Sem variável `JAVA_TOOL_OPTIONS` para limites de memória da JVM

**Repo:** `backend/docker-compose.yml`
**Severidade:** 🟡 Baixa

A `JAVA_TOOL_OPTIONS` está definida apenas para fuso horário. Em containers, a JVM pode tentar alocar memória baseada no host (não no limite do container), causando OOM kills.

**Recomendação:** Adicionar `-XX:MaxRAMPercentage=75.0` para que a JVM respeite os limites do cgroup do container.

---

## 6. 🐛 Bugs

### 6.1 — Bug em `AgendamentoService.deletar()` — iteração com `for` que pulou `s3StorageService.delete`

**Repo:** `backend/kore`
**Arquivo:** [`AgendamentoService.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/service/AgendamentoService.java#L92-L98)
**Severidade:** 🔴 Alta

```java
for (Foto foto : fotos)
    // this.s3StorageService.delete(foto.getNome());  ← COMENTADO

this.fotoRepository.deleteAll(fotos);
this.fotoRepository.deleteAllByAgendamento(agendamento);  // ← chamada duplicada
```

O `for` **sem bloco `{}`** na linha 94 significa que `this.s3StorageService.delete` está comentado, e `fotoRepository.deleteAll(fotos)` é o corpo real do `for` — logo a deleção do repositório é executada `fotos.size()` vezes (uma por iteração) antes de `deleteAllByAgendamento` chamar a deleção novamente. Além disso, as imagens do S3 **nunca são deletadas** ao excluir um agendamento, causando vazamento de storage.

**Recomendação:** Corrigir o `for` com bloco `{}`, descomentar o `s3StorageService.delete` e remover a chamada duplicada a `deleteAllByAgendamento`.

---

### 6.2 — `GET /agendamentos` sem parâmetros causa `NullPointerException`

**Repo:** `backend/kore`
**Arquivo:** [`AgendamentoController.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/controller/AgendamentoController.java#L44-L52)
**Severidade:** 🔴 Alta

```java
public ResponseEntity<List<AgendamentoResponseDTO>> listar(
    @RequestParam(name = "inicio", required = false) String inicio,
    @RequestParam(name = "fim", required = false) String fim,
    ...
) {
    return ResponseEntity.ok(agendamentoService.listarEntreDatas(
        LocalDateTime.parse(inicio, ...),  // ← NPE se inicio == null
        LocalDateTime.parse(fim, ...),     // ← NPE se fim == null
```

Os parâmetros `inicio` e `fim` são `required = false`, mas são passados diretamente ao `LocalDateTime.parse()` sem nenhuma verificação de nulidade. Uma requisição `GET /agendamentos` sem parâmetros causa NPE com 500.

**Recomendação:** Validar os parâmetros ou torná-los `required = true`.

---

### 6.3 — `Produto.adicionarEstoque()` sempre lança `ConcurrentModificationException`

**Repo:** `backend/kore`
**Arquivo:** [`Produto.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/model/Produto.java#L77-L79)
**Severidade:** 🔴 Alta (código morto com crash garantido)

```java
public void adicionarEstoque(List<Item> itens) {
    itens.forEach(item -> itens.add(item)); // ← modifica a coleção durante iteração
}
```

Já mencionado em Arquitetura, mas reforçado aqui como bug de runtime. Se esse método for chamado, **sempre** lança `ConcurrentModificationException`.

---

### 6.4 — `NotificacaoController.listar()` recebe `Usuario` mas não o usa

**Repo:** `backend/kore`
**Arquivo:** [`NotificacaoController.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/controller/NotificacaoController.java#L38-L41)
**Severidade:** 🔴 Alta (causa de IDOR — ver Segurança 2.7)

```java
public ResponseEntity<List<Notificacao>> listar(@AuthenticationPrincipal Usuario usuario) {
    List<Notificacao> notificacoes = notificacaoRepository.findAll(); // usuario ignorado
```

O parâmetro `usuario` é injetado mas completamente ignorado — `findAll()` retorna tudo sem filtrar pelo usuário logado.

---

### 6.5 — `EstoqueValidadeScheduler`: mensagem de item vencido usa texto de "estoque mínimo" (cópia errada)

**Repo:** `backend/kore`
**Arquivo:** [`EstoqueValidadeScheduler.java`](file:///home/henry/Projetos/Faculdade/kore-sptech/backend/kore/src/main/java/kore/backend/events/EstoqueValidadeScheduler.java#L94-L97)
**Severidade:** 🟡 Média

```java
// Bloco de itens VENCIDOS
String.format("O estoque de %s atingiu o nível mínimo. Restam apenas %d unidades.",
        nomeProduto, itens.size()),
```

A mensagem enviada para itens **vencidos** fala em "atingiu o nível mínimo", que é a mensagem de estoque baixo — não de item vencido. Parece um copy-paste de outro bloco.

**Recomendação:** Corrigir para: `"O produto %s tem %d unidade(s) VENCIDA(S) ainda ativa(s). Remova-as imediatamente."`

---

### 6.6 — `useWeekSchedule` não reseta a lista de sessões ao trocar de semana

**Repo:** `front-end`
**Arquivo:** [`useWeekSchedule.js`](file:///home/henry/Projetos/Faculdade/kore-sptech/front-end/src/features/scheduling/useWeekSchedule.js#L39-L60)
**Severidade:** 🟡 Média

Ao trocar `selectedDate`, o `useEffect` dispara a nova requisição, mas `sessions` só é atualizado com `setSessions(response.data)` após a resposta. Durante o carregamento, a tela exibe os agendamentos da **semana anterior** até a nova resposta chegar, sem indicação visual de carregamento.

**Recomendação:** Adicionar `setSessions([])` ou um estado `isLoading` no início do `useEffect` antes de fazer a requisição.

---

## Resumo de Priorização

| Prioridade | Issue | Impacto |
|---|---|---|
| 🔴 **P0 — Crítico** | Banco de dados sem senha em produção (5.1 / 2.1) | Comprometimento total do banco |
| 🔴 **P0 — Crítico** | HTTPS ausente no Nginx (5.5) | Tokens JWT em texto puro |
| 🔴 **P0 — Crítico** | IDOR em notificações (2.7 / 6.4) | Vazamento de dados entre usuários |
| 🔴 **P1 — Alta** | JWT Secret com fallback hardcoded (2.2) | Tokens forjáveis |
| 🔴 **P1 — Alta** | SSE sem autenticação e multi-tenant (1.4) | Vazamento de notificações |
| 🔴 **P1 — Alta** | Bug no `deletar()` — S3 nunca limpo (6.1) | Storage leak em produção |
| 🔴 **P1 — Alta** | NPE no `GET /agendamentos` sem parâmetros (6.2) | 500 em uso normal |
| 🟡 **P2 — Média** | Versões divergentes de Spring Boot (4.1) | Instabilidade e incompatibilidade |
| 🟡 **P2 — Média** | Dois microsserviços no mesmo schema (1.1) | Acoplamento de banco |
| 🟡 **P2 — Média** | `NotificacaoController` acessando repositório diretamente (1.3) | Manutenção difícil |
| 🟡 **P2 — Média** | Sem volume persistente no RabbitMQ (5.3) | Perda de mensagens ao reiniciar |
| 🟢 **P3 — Baixa** | Dockerfiles duplicados (3.1) | Manutenção duplicada |
| 🟢 **P3 — Baixa** | `SEED_NOTIFICATIONS` hardcoded no frontend (3.4) | Dados desconectados da realidade |

---

*Relatório gerado por análise estática do código. Recomenda-se revisão em par antes de priorizar execução.*
