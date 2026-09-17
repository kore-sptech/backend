# Kore

Kore é uma plataforma de gestão para salões e estúdios, desenvolvida como projeto de faculdade em arquitetura de monorepo. O sistema reúne gestão de usuários, agendamentos, produtos, estoque, transações financeiras, autenticação e notificações em tempo real.

## Visão geral

O projeto está dividido em três partes principais:

- backend/kore: API principal da aplicação
- backend/notification: microsserviço de notificações
- front-end: interface web em React + Vite

A solução também conta com um ambiente de testes em `test/backend`, com suporte a containers para integração automatizada.

## Stack tecnológica

| Camada                        | Tecnologias                                                  |
| ----------------------------- | ------------------------------------------------------------ |
| Backend principal             | Java 21, Spring Boot 4.0.3, Maven, Spring Security, JPA, JWT |
| Microsserviço de notificações | Java 21, Spring Boot 4.1.1, RabbitMQ, JPA                    |
| Frontend                      | React 19, Vite 8, Tailwind CSS 4, DaisyUI                    |
| Banco de dados                | MySQL                                                        |
| Mensageria                    | RabbitMQ                                                     |
| Arquivos                      | AWS S3 (configurável)                                        |
| Testes                        | JUnit, Spring Test, Testcontainers                           |
| Documentação de API           | Swagger / OpenAPI                                            |

## Arquitetura

### Backend principal

A API principal segue a estrutura em camadas:

```text
controller -> service -> repository -> model (JPA entities)
```

Principais responsabilidades:

- autenticação com JWT
- cadastro e gestão de usuários
- gestão de produtos, categorias e estoque
- agendamentos, transações e finanças
- geração e persistência de notificações
- envio de eventos via SSE para atualização em tempo real

### Microsserviço de notificações

O serviço de notificações segue um modelo mais enxuto e orientado a casos de uso:

```text
adapters/entities -> adapters/repositories -> application/useCases -> entities
```

Ele escuta eventos enviados pela API principal via RabbitMQ e processa as notificações de forma desacoplada.

### Frontend

A interface web é uma SPA em React com rotas protegidas, contexto para estados globais e comunicação com o backend via Axios e SSE.

## Funcionalidades principais

- autenticação e autorização com JWT
- cadastro e login de usuários
- painel administrativo e financeiro
- agendamentos e controle de sessões
- catálogo de produtos, categorias e estoque
- registro de transações
- notificações em tempo real
- upload de fotos de produtos
- dashboard com métricas e indicadores
- documentação da API via Swagger

## Estrutura do repositório

```text
kore-sptech/
├── README.md
├── backend/
│   ├── .env.example
│   ├── docker-compose.yml
│   ├── docker-compose.dev.yml
│   ├── README-DOCKER.md
│   ├── kore/
│   │   ├── Dockerfile
│   │   ├── mvnw
│   │   └── src/
│   ├── notification/
│   │   ├── Dockerfile
│   │   ├── mvnw
│   │   └── src/
│   └── ...
```

## Requisitos

Antes de rodar o projeto, verifique se você possui:

- Docker e Docker Compose
- Java 21
- Maven
- Node.js 20+ e npm
- Git

## Configuração de ambiente

Crie o arquivo de ambiente do backend com base no exemplo:

```bash
cd backend
cp .env.example .env
```

Variáveis importantes:

- `JWT_SECRET`: chave secreta para assinatura dos tokens
- `FRONT_END_URL`: origem permitida para CORS / SSE
- `AWS_*`: credenciais do S3 para upload de imagens
- `RABBITMQ_DEFAULT_USER` e `RABBITMQ_DEFAULT_PASS`: usuário e senha do RabbitMQ

## Executando o projeto

### Opção recomendada: Docker

Na raiz do backend:

```bash
cd backend

# Ambiente de desenvolvimento com hot reload
 docker compose -f docker-compose.dev.yml up --build
```

Esse ambiente sobe:

- API principal em `http://localhost:8080`
- Microsserviço de notificações em `http://localhost:8081`
- MySQL em `localhost:3306`
- RabbitMQ em `localhost:5672`
- RabbitMQ Management em `http://localhost:15672` (usuário: `guest`, senha: `guest`)
- Porta de debug em `localhost:5005`

### Executando sem Docker

Para rodar a API principal localmente, será necessário ter o MySQL em execução e ajustar as variáveis de ambiente:

```bash
cd backend/kore
./mvnw spring-boot:run
```

### Frontend

```bash
cd front-end
npm install
npm run dev
```

A aplicação frontend fica em:

```text
http://localhost:5173
```

## Swagger e documentação da API

A aplicação principal expõe a documentação OpenAPI/Swagger em:

```text
http://localhost:8080/swagger-ui.html
```

## Comunicação em tempo real

O backend principal expõe o fluxo SSE em:

```text
/sse/stream
```

O frontend se conecta a esse endpoint via `NotificationProvider`, permitindo notificações instantâneas sem polling constante.

## Mensageria

A comunicação entre os serviços utiliza RabbitMQ. A fila principal é:

```text
notification-queue
```

A API principal publica eventos de notificação e o microsserviço de notificações os consome.

## Testes

### Backend principal

```bash
cd backend/kore
./mvnw test
```

Testes do projeto usam Testcontainers e exigem Docker em execução.

### Frontend

```bash
cd front-end
npm run lint
npm run build
```

## Comandos úteis

### Backend principal

```bash
cd backend/kore
./mvnw clean install
./mvnw test
./mvnw spring-boot:run
```

### Microsserviço de notificação

```bash
cd backend/notification
./mvnw clean install
./mvnw spring-boot:run
```

### Frontend

```bash
cd front-end
npm install
npm run dev
npm run build
npm run preview
```

## Observações importantes

- O projeto usa arquitetura de monorepo com serviços independentes, mas integrados.
- O backend principal e o microsserviço de notificações possuem configurações próprias e dependem de variáveis de ambiente e de infraestrutura externa.
- O armazenamento de fotos foi configurado para o diretório do frontend em `front-end/public/uploads` para facilitar o desenvolvimento local.
- O projeto foi pensado para desenvolvimento em ambiente local e integração com Docker, preservando fluxo de hot reload e debug.

## Licença

Este projeto foi desenvolvido como parte de atividade acadêmica e não foi definido um modelo de licença específico até o momento.
