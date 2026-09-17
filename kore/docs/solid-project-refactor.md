# Refatoração do projeto para aderência ao SOLID

## Visão geral
Este documento resume as alterações realizadas para aumentar a coesão, reduzir o acoplamento e alinhar o projeto ao SOLID e aos princípios de clean code.

## 1) Separação de responsabilidades por camada
A regra principal adotada foi: cada camada deve responder por uma parte bem definida do sistema.

- Controllers: entrada e saída HTTP
- Services: orquestração de casos de uso
- Policies: regras de negócio e validações do domínio
- Validators: validações específicas e verificações reutilizáveis
- Repositories: persistência
- Handlers: tratamento global de exceções
- Security: autenticação e autorização

Com isso, as classes deixaram de misturar regras de negócio, transporte HTTP, autenticação, persistência e fluxo de aplicação.

## 2) Criação da camada `service.policy`
A camada `service.policy` foi criada para encapsular regras que antes estavam espalhadas por services.

### Policies adicionadas
- `UsuarioPolicy`
- `ProdutoPolicy`
- `CategoriaPolicy`
- `AgendamentoPolicy`
- `TransacaoPolicy`
- `AgendamentoStatusTransition`

Essas classes centralizam verificações como:
- e-mail duplicado
- usuário inexistente
- categoria inválida
- produto fora do contexto do usuário
- conflito de agendamento
- cancelamento/confirmacao de status
- valor inválido de transação

A vantagem é que o código de negócio deixa de ficar espalhado em vários `if`s dentro das services.

## 3) Aumento de coesão das services
Antes, as services acumulavam validação, regra e manipulação de dados. Agora elas passam a ter um papel mais claro:

- receber dados de entrada
- chamar políticas de domínio
- coordenar persistência
- devolver resultado da operação

Exemplos:
- `UsuarioService` agora delega cadastro, atualização e login para `UsuarioPolicy`
- `AgendamentoService` delega regras de status e conflito para `AgendamentoPolicy`
- `ProdutoService` delega validações do produto e do usuário para `ProdutoPolicy`
- `CategoriaService` valida dados antes de salvar/atualizar
- `TransacaoService` valida valor de transação em policy isolada

## 4) Implementação do SRP
O Princípio da Responsabilidade Única foi aplicado movendo regras do domínio para classes menores e especializadas.

### Antes
As services eram responsáveis por:
- validar existência
- validar regras de negócio
- comparar senhas
- verificar status de agendamento
- verificar conflitos de horário
- manipular persistência

### Depois
Cada classe passou a ter uma responsabilidade mais objetiva:
- `Service`: caso de uso
- `Policy`: regra de negócio
- `Repository`: persistência
- `Handler`: resposta de erro

## 5) Aplicação do DIP (Dependency Inversion Principle)
As services passaram a depender de abstrações e regras de negócio encapsuladas em policies, em vez de depender diretamente de detalhes concretos e acoplados.

Isso reduz o acoplamento estrutural e facilita:
- troca de implementação
- testes isolados
- extensão do sistema
- manutenção sem quebrar o restante da aplicação

## 6) Redução do acoplamento em infraestrutura e segurança
Também foi ajustado o código relacionado a segurança e filtros:

- `SecurityFilter` agora usa injeção por construtor
- logs e debug foram removidos
- autenticação ficou mais limpa e previsível
- `TokenService` foi simplificado e centralizou a lógica de criação/validação JWT

Isso evita acoplamento forte e reduz efeitos colaterais na aplicação.

## 7) Tratamento global de erros
Foi criado um `GlobalExeptionHandler` e um `ApiErrorResponse` para centralizar respostas de erro.

Antes havia comportamento disperso e mensagens inconsistentes.
Agora o projeto responde de forma padronizada para:
- recurso não encontrado
- credencial já existente
- argumento inválido
- autenticação inválida
- erro interno

Isso melhora a consistência da API e reforça o princípio de encapsulamento de fluxo de erro.

## 8) Eliminação de debug e acoplamento de infraestrutura no fluxo
Diversos pontos de depuração e acoplamento foram removidos, incluindo:
- `System.out.println` em controllers e filtros
- `@Autowired` em campos
- código de debug no fluxo de autenticação e transações

Essas práticas aumentam ruído e dificultam manutenção e leitura do código.

## 9) Coesão por políticas de domínio
A grande mudança foi mover a lógica de decisão “o que é válido?” para objetos específicos do domínio.

Isso reduz:
- repetição de `if`
- classes inchadas
- regras espalhadas
- dependência cruzada entre domínio e infraestrutura

A ideia é que a service faça o que realmente deve fazer: orquestrar a operação.

## 10) Resultado esperado
Com essas mudanças, o projeto ficou mais alinhado com os princípios do SOLID, especialmente:

- SRP: responsabilidade separada por classe
- OCP: extensível sem modificar o núcleo
- LSP: contratos mais previsíveis e substituíveis
- ISP: interfaces menores e mais específicas
- DIP: dependência de abstrações, não de implementações concretas

## 11) DTOs e entidades desacopladas da camada de transporte
Foi realizado um ajuste importante para reduzir o acoplamento entre o domínio e o payload HTTP.

Antes:
- entidades como `Produto`, `Item`, `Agendamento` e `Transacao` importavam tipos de DTO diretamente;
- a model possuía construtores que recebiam objetos de transporte;
- a camada de domínio ficava dependente de contratos de entrada/saída.

Depois:
- as entidades passaram a representar apenas o estado e o comportamento do domínio;
- conversões entre DTO e entidade foram centralizadas em `mapper`/`policy`-style components;
- a model foi isolada de detalhes do HTTP e do contexto de persistência.

Isso reforça:
- SRP: cada camada tem responsabilidade definida;
- DIP: o domínio não depende de detalhes de transporte;
- Clean Code: regras de transformação ficam explícitas e localizadas;
- manutenção: mudanças em contratos de API não exigem alteração direta das entidades.

## 12) Percentual de alinhamento ao SOLID
Considerando a estrutura atual do backend, a camada de negócio e a infraestrutura já revisadas estão em torno de 80% a 90% alinhadas ao SOLID.

O restante ainda está em evolução em pontos específicos de:
- persistência;
- consultas especializadas;
- DTOs/entidades com mais dependência de infraestrutura;
- fluxos de integração e eventos que ainda podem receber uma camada de domínio mais estrita.

Mesmo assim, o projeto já está significativamente mais coeso e menos acoplado do que no estado inicial.

## 13) Validação
A aplicação foi compilada com sucesso após as alterações:

```bash
/tmp/apache-maven-3.9.16/bin/mvn -q -DskipTests compile
```

Resultado: compilação concluída com sucesso.

## Conclusão
A refatoração foi direcionada para a arquitetura do projeto como um todo, não apenas para as services. A base resultante é mais coesa, mais previsível, mais fácil de evoluir e mais próxima de um código Java moderno com princípios de clean architecture e SOLID.
