# syntax=docker/dockerfile:1

##########################################################################
# STAGE: base -> baixa as dependências do Maven (cache reaproveitado
# entre dev e prod, e entre builds enquanto o pom.xml não mudar)
##########################################################################
FROM maven:3.9-eclipse-temurin-21 AS base
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline

##########################################################################
# STAGE: dev -> imagem usada pelo docker-compose.dev.yml (live reload)
#
# O código-fonte vem via bind mount (montado em /app pelo compose), então
# aqui não copiamos nada do build context. A aplicação sobe com
# `mvn spring-boot:run`; como o spring-boot-devtools está no classpath,
# o plugin automaticamente roda em processo forkado e reinicia o contexto
# Spring sempre que o classpath (target/classes) muda.
#
# Só que salvar um .java no host não recompila nada sozinho — por isso
# rodamos em paralelo um watcher (inotifywait) que executa `mvn compile`
# a cada alteração em src/, o que atualiza o classpath e dispara o
# restart do devtools. É esse par (spring-boot:run + watcher de compile)
# que dá o efeito de live reload.
##########################################################################
FROM base AS dev
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends inotify-tools \
    && rm -rf /var/lib/apt/lists/*

COPY docker/dev-entrypoint.sh /usr/local/bin/dev-entrypoint.sh
RUN chmod +x /usr/local/bin/dev-entrypoint.sh

EXPOSE 8080
# porta de debug remoto opcional (attach do IDE)
EXPOSE 5005

ENTRYPOINT ["/usr/local/bin/dev-entrypoint.sh"]

##########################################################################
# STAGE: build -> gera o jar final para produção
##########################################################################
FROM base AS build
WORKDIR /app
COPY . .
RUN mvn -B -o clean package -DskipTests

##########################################################################
# STAGE: prod -> imagem enxuta, só com JRE + jar, sem Maven/toolchain
##########################################################################
FROM eclipse-temurin:21-jre-alpine AS prod
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
