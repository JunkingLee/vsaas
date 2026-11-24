#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "[1/3] docker middleware"
(cd docker && docker compose up -d)

echo "[2/3] wait postgres"
for i in {1..30}; do
  if docker exec vsaas-postgres pg_isready -U vsaas >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

echo "[3/3] apply sql if needed (idempotent)"
docker exec -i vsaas-postgres psql -U vsaas -d vsaas < sql/01-schema-core.sql || true
docker exec -i vsaas-postgres psql -U vsaas -d vsaas < sql/02-schema-billing-rbac.sql || true
docker exec -i vsaas-postgres psql -U vsaas -d vsaas < sql/seed-demo.sql || true

echo "Next: mvn -pl vsaas-auth -am spring-boot:run (and other modules)"
echo "Frontend: cd frontend && npm i && npm run dev"
echo "Docs: CLOSED_LOOP.md"
