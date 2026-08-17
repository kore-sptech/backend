# Containerização do backend (kore/backend)

Projeto Spring Boot 4 / Java 21 / Maven / MySQL. Arquivos adicionados:

```
Dockerfile                  # multi-stage: base, dev, build, prod
docker-compose.yml          # produção (app + mysql)
docker-compose.dev.yml      # desenvolvimento com live reload (app + mysql)
docker/dev-entrypoint.sh    # script usado só pelo target "dev"
.dockerignore
```

Também houve **duas alterações pequenas no código existente**:

- `pom.xml`: adicionada a dependência `spring-boot-devtools` (escopo `optional`).
  Só é usada em dev, é automaticamente excluída do jar de produção pelo
  `spring-boot-maven-plugin` (comportamento padrão do Spring Boot).
- `application.properties`: `spring.datasource.url/username/password` e
  `front-end.url` agora leem de variáveis de ambiente, com os mesmos valores
  de antes como default — ou seja, se você rodar sem Docker, sem exportar
  nada, continua igual a antes (`localhost:3306`, usuário `root`, etc).

## Produção

```bash
docker compose up --build
```

Sobe o MySQL e a API compilada (jar), expostos em `localhost:8080`.
A imagem final é baseada em `eclipse-temurin:21-jre-alpine`, sem Maven,
rodando como usuário não-root.

Variáveis que dá pra sobrescrever (todas opcionais, têm default):

```bash
JWT_SECRET=algum-segredo FRONT_END_URL=https://meu-front.com docker compose up --build
```

## Desenvolvimento (live reload)

```bash
docker compose -f docker-compose.dev.yml up --build
```

O código-fonte é montado do seu host para dentro do container (bind mount).
Um script observa a pasta `src/` com `inotifywait`: a cada arquivo salvo,
ele roda `mvn compile`, o que atualiza o classpath e aciona o restart
automático do `spring-boot-devtools` (a aplicação já está rodando via
`mvn spring-boot:run`). Não precisa rebuildar a imagem nem reiniciar o
container para ver as mudanças — só salvar o arquivo `.java`.

Também expõe a porta `5005` para debug remoto (attach do IDE via JDWP).

Na primeira subida, o container baixa as dependências Maven pra dentro do
volume nomeado `maven_repo` (fica lento na primeira vez). Nas próximas
subidas, esse cache já está populado e fica bem mais rápido.

## Sobre a pasta de uploads

O `UploadFotoController` salva arquivos em `../front-end/public/uploads/`
(caminho relativo, pensado para quando o backend roda como parte de um
monorepo ao lado de uma pasta `front-end`). Dentro do container isso
resolve para `/front-end/public/uploads`, e por isso ambos os compose
files montam um volume nesse caminho (`uploads` em prod, `uploads_dev`
em dev) — assim os arquivos enviados não se perdem quando o container é
recriado.

## A partir do Git

O conteúdo do zip enviado é idêntico ao repositório
`https://github.com/kore-sptech/backend.git` (branch padrão). Então dá
para clonar o repo, aplicar estes mesmos arquivos (Dockerfile,
docker-compose*.yml, pasta docker/, e os dois ajustes no pom.xml e no
application.properties) e funciona da mesma forma.
