#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WEB_DIR="$ROOT_DIR/web-user"

BACK_PID=""
FRONT_PID=""

BACK_HOST="${BACK_HOST:-127.0.0.1}"
BACK_PORT="${BACK_PORT:-8080}"
FRONT_HOST="${FRONT_HOST:-127.0.0.1}"
FRONT_PORT="${FRONT_PORT:-5173}"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-pet_platform}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-Littleha233!}"

FORCE_RESTART="${FORCE_RESTART:-0}"

cleanup() {
  if [[ -n "${FRONT_PID}" ]] && kill -0 "${FRONT_PID}" 2>/dev/null; then
    kill "${FRONT_PID}" 2>/dev/null || true
  fi
  if [[ -n "${BACK_PID}" ]] && kill -0 "${BACK_PID}" 2>/dev/null; then
    kill "${BACK_PID}" 2>/dev/null || true
  fi
}

trap cleanup INT TERM EXIT

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1"
    exit 1
  fi
}

ensure_port_free() {
  local port="$1"
  local owner_pids
  owner_pids="$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -z "${owner_pids}" ]]; then
    return 0
  fi

  if [[ "${FORCE_RESTART}" == "1" ]]; then
    echo "Port ${port} is in use, killing: ${owner_pids}"
    while IFS= read -r pid; do
      [[ -n "${pid}" ]] && kill -9 "${pid}" 2>/dev/null || true
    done <<< "${owner_pids}"
    return 0
  fi

  echo "Port ${port} is already in use: ${owner_pids}"
  echo "Set FORCE_RESTART=1 to auto-kill and restart."
  exit 1
}

require_cmd lsof
require_cmd curl
require_cmd npm
require_cmd mysqladmin

if [[ ! -x "$ROOT_DIR/mvnw" ]]; then
  echo "Cannot find executable mvnw at: $ROOT_DIR/mvnw"
  exit 1
fi

if [[ ! -d "$WEB_DIR" ]]; then
  echo "Cannot find frontend dir: $WEB_DIR"
  exit 1
fi

ensure_port_free "${BACK_PORT}"
ensure_port_free "${FRONT_PORT}"

if [[ "${SKIP_DB_CHECK:-0}" != "1" ]]; then
  if ! mysqladmin -h "${DB_HOST}" -P "${DB_PORT}" -u"${DB_USERNAME}" --password="${DB_PASSWORD}" ping --silent >/dev/null 2>&1; then
    echo "Cannot connect MySQL: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
    echo "Please start MySQL first, then rerun this script."
    echo "Tip: if you run with docker-compose, start it via: docker compose up -d mysql"
    exit 1
  fi
fi

if [[ ! -d "$WEB_DIR/node_modules" ]]; then
  echo "Installing frontend dependencies..."
  (cd "$WEB_DIR" && npm install)
fi

echo "Starting backend on http://${BACK_HOST}:${BACK_PORT} ..."
(
  cd "$ROOT_DIR"
  DB_HOST="${DB_HOST}" \
  DB_PORT="${DB_PORT}" \
  DB_NAME="${DB_NAME}" \
  DB_USERNAME="${DB_USERNAME}" \
  DB_PASSWORD="${DB_PASSWORD}" \
  ./mvnw -q spring-boot:run
) &
BACK_PID="$!"

echo "Waiting backend health..."
for _ in $(seq 1 60); do
  if curl -fsS "http://${BACK_HOST}:${BACK_PORT}/api/v1/system/health" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! curl -fsS "http://${BACK_HOST}:${BACK_PORT}/api/v1/system/health" >/dev/null 2>&1; then
  echo "Backend health check failed."
  exit 1
fi

echo "Starting frontend on http://${FRONT_HOST}:${FRONT_PORT} ..."
(
  cd "$WEB_DIR"
  VITE_API_BASE_URL="http://${BACK_HOST}:${BACK_PORT}" \
  npm run dev -- --host "${FRONT_HOST}" --port "${FRONT_PORT}"
) &
FRONT_PID="$!"

echo "All services started:"
echo "  Backend : http://${BACK_HOST}:${BACK_PORT}"
echo "  Frontend: http://${FRONT_HOST}:${FRONT_PORT}"
echo "Press Ctrl+C to stop both."

while true; do
  if ! kill -0 "${BACK_PID}" 2>/dev/null; then
    wait "${BACK_PID}" || true
    echo "Backend exited unexpectedly."
    exit 1
  fi
  if ! kill -0 "${FRONT_PID}" 2>/dev/null; then
    wait "${FRONT_PID}" || true
    echo "Frontend exited unexpectedly."
    exit 1
  fi
  sleep 1
done
