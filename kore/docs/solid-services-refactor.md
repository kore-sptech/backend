# Refatoração de services para aderência ao SOLID

## Objetivo
Ajustar a camada de services do backend para reduzir acoplamento, separar regras de negócio e infraestrutura, padronizar validações e deixar o código mais fácil de manter e testar.

## O que foi feito

### 1) Separação de regras de validação em contratos específicos
Foi criada a camada `kore.backend.service.validation` com interfaces e implementações para cada domínio principal:

- `UsuarioValidationService`
- `CategoriaValidationService`
- `ProdutoValidationService`
- `AgendamentoValidationService`
- `TransacaoValidationService`
- `ItemValidationService`

Essas classes centralizam a lógica de:
- existência de usuário/categoria/produto
- email duplicado
- conflito de agendamento
- período inválido
- valor inválido de transação
- resolução de dependências de domínio

Isso reduz a responsabilidade das services e fortalece o Princípio da Responsabilidade Única (SRP).

### 2) Inversão de dependência aplicada às services principais
As services passaram a depender de interfaces de validação e não diretamente de implementações concretas de regras e repositórios do domínio.

Exemplos:
- `UsuarioService` depende de `UsuarioValidationService`
- `CategoriaService` depende de `CategoriaValidationService`
- `ProdutoService` depende de `CategoriaValidationService` e `ProdutoValidationService`
- `AgendamentoService` depende de `AgendamentoValidationService`
- `TransacaoService` depende de `TransacaoValidationService`
- `ItemService` depende de `ItemValidationService`

Esse ajuste é a base do Princípio da Inversão de Dependência (DIP): a camada de negócio depende de abstrações e não de implementações acopladas.

### 3) Correção do contrato de validação para LSP e coesão
Foi corrigida a estrutura de validação para deixar os contratos mais coerentes e substituíveis:

- `UsuarioExisteValidacao` e `CategoriaExisteValidacao` passaram a atuar como regras de validação mais previsíveis
- `ProdutoService` deixou de depender de validação acoplada a consulta e persistência
- `UsuarioService` passou a centralizar verificação de email e existência em um único serviço de validação

Com isso, o comportamento esperado se mantém consistente para qualquer implementação equivalente da interface.

### 4) Ajuste de `UsuarioService` para autenticação segura
A autenticação foi ajustada para usar `PasswordEncoder.matches(...)`, em vez de comparar senhas em texto puro.

Também foi removido o uso de `@Autowired` em campo e a classe foi convertida para depender de construtor.

### 5) Redução de regras embutidas em services
Antes, algumas services acumulavam:
- verificação de existência
- regras de negócio
- consulta ao banco
- criação de objetos
- manipulação de estado

Agora, a maior parte dessas regras foi movida para serviços de validação específicos, deixando cada service mais focada no fluxo do caso de uso.

### 6) Padronização de comportamento para regras de negócio
Foram padronizados pontos como:
- verificação de usuário inexistente
- categoria inexistente
- produto não pertencente ao usuário
- conflito de horário em agendamento
- valor inválido de transação

## Principais princípios atendidos

### SRP (Single Responsibility Principle)
As services ficaram mais focadas em orquestrar casos de uso, enquanto as validações específicas ficaram isoladas em classes especializadas.

### OCP (Open/Closed Principle)
Novas regras podem ser adicionadas sem mexer na lógica principal das services, bastando criar uma nova implementação de validação ou outra estratégia.

### LSP (Liskov Substitution Principle)
Os contratos de validação ficaram mais consistentes e previsíveis, com comportamentos equivalentes entre implementações.

### ISP (Interface Segregation Principle)
Cada interface foi criada para um conjunto específico de regras. Não existe um contrato grande demais com responsabilidades misturadas.

### DIP (Dependency Inversion Principle)
As services passam a depender de abstrações (`validation services`) em vez de dependerem diretamente de detalhes concretos de infraestrutura.

## Serviços impactados
- `UsuarioService`
- `CategoriaService`
- `ProdutoService`
- `AgendamentoService`
- `TransacaoService`
- `ItemService`
- `AuthService`

## Verificação
Foi feita compilação do projeto com Maven, sem executar testes:

```bash
/tmp/apache-maven-3.9.16/bin/mvn -q -DskipTests compile
```

Resultado: compilação concluída com sucesso.

## Observação importante
A refatoração estruturou a camada de services em um padrão muito mais alinhado ao SOLID, especialmente em SRP, DIP e LSP. A arquitetura continua aberta para evolução e conta com validações independentes, sem acoplamento pesado entre regras e infraestrutura.
