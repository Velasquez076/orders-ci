#!/usr/bin/env bash
# Generates evidence files with a visible DATE. Run from the repository root.
# Usage: ./scripts/evidencias.sh tests | docker
set -u
# Always run from the repository root, whatever the current directory is.
cd "$(dirname "$0")/.." || exit 1
mkdir -p evidencias
stamp() { echo "=== $(date '+%Y-%m-%d %H:%M:%S %Z') | $(whoami)@$(hostname) ==="; }

case "${1:-}" in
  tests)
    { stamp; mvn -B test; } 2>&1 | tee evidencias/01_local_tests.txt ;;
  docker)
    { stamp
      docker --version
      docker build --target tests -t orders-tests .
      docker images orders-tests
      docker run --rm --name orders-tests-ctr orders-tests
    } 2>&1 | tee evidencias/05_docker_tests.txt ;;
  *)
    echo "Usage: $0 tests | docker"; exit 1 ;;
esac
