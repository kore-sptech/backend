#!/bin/sh
set -e

cd /app

# Nota sobre o cache do Maven: o volume nomeado "maven_repo" (montado em
# /root/.m2 pelo compose) pode ficar populado, mas ainda assim não conter
# uma dependência recém-adicionada. Nesse caso, o modo offline quebra a
# primeira subida. Por isso tentamos offline primeiro e, se falhar, refazemos
# online para atualizar o cache.
MVN_CAN_TRY_OFFLINE=1

if [ -z "$(ls -A /root/.m2 2>/dev/null)" ]; then
  echo "[dev-entrypoint] cache do Maven (/root/.m2) vazio, baixando dependências online..."
  MVN_CAN_TRY_OFFLINE=0
fi

run_maven_with_fallback() {
  if [ "$MVN_CAN_TRY_OFFLINE" -eq 1 ]; then
    if mvn -o "$@"; then
      return 0
    fi

    echo "[dev-entrypoint] falha no modo offline, tentando online..."
  fi

  mvn "$@"
}

echo "[dev-entrypoint] compilando pela primeira vez..."
run_maven_with_fallback compile -q

echo "[dev-entrypoint] iniciando aplicação (mvn spring-boot:run)..."
run_maven_with_fallback spring-boot:run \
    -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" &
APP_PID=$!

echo "[dev-entrypoint] observando alterações em src/ para live reload..."
while inotifywait -r -e modify,create,delete,move src pom.xml; do
  echo "[dev-entrypoint] mudança detectada, recompilando..."
  run_maven_with_fallback compile -q || echo "[dev-entrypoint] falha ao compilar, aguardando próxima mudança"
done &
WATCH_PID=$!

wait $APP_PID $WATCH_PID
