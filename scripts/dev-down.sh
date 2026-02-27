#!/usr/bin/env bash
set -euo pipefail

BACK_PORT="${BACK_PORT:-8080}"
FRONT_PORT="${FRONT_PORT:-5173}"

stop_port() {
  local port="$1"
  local pids
  pids="$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -z "${pids}" ]]; then
    echo "Port ${port}: no running process."
    return 0
  fi

  echo "Port ${port}: stopping ${pids}"
  while IFS= read -r pid; do
    [[ -n "${pid}" ]] && kill "${pid}" 2>/dev/null || true
  done <<< "${pids}"

  sleep 1
  pids="$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -n "${pids}" ]]; then
    echo "Port ${port}: force killing ${pids}"
    while IFS= read -r pid; do
      [[ -n "${pid}" ]] && kill -9 "${pid}" 2>/dev/null || true
    done <<< "${pids}"
  fi
}

stop_port "${BACK_PORT}"
stop_port "${FRONT_PORT}"

echo "Done."
