#!/bin/sh
set -e

cd /app

# Nota sobre o cache do Maven: o volume nomeado "maven_repo" (montado em
# /root/.m2 pelo compose) começa vazio na primeira execução, mesmo que a
# imagem já tenha baixado as dependências durante o build (aquele cache
# fica em outra camada da imagem, não neste volume). Por isso a primeira
# subida roda ONLINE para popular o volume; nas próximas, com o volume já
# populado, dá pra usar -o (offline) e ficar bem mais rápido.
MVN_MODE="-o"
if [ -z "$(ls -A /root/.m2 2>/dev/null)" ]; then
  echo "[dev-entrypoint] cache do Maven (/root/.m2) vazio, baixando dependências..."
  MVN_MODE=""
fi

echo "[dev-entrypoint] compilando pela primeira vez..."
mvn $MVN_MODE compile -q

echo "[dev-entrypoint] iniciando aplicação (mvn spring-boot:run)..."
mvn $MVN_MODE spring-boot:run \
    -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" &
APP_PID=$!

echo "[dev-entrypoint] observando alterações em src/ para live reload..."
while inotifywait -r -e modify,create,delete,move src pom.xml; do
  echo "[dev-entrypoint] mudança detectada, recompilando..."
  mvn -o compile -q || echo "[dev-entrypoint] falha ao compilar, aguardando próxima mudança"
done &
WATCH_PID=$!

wait $APP_PID $WATCH_PID
