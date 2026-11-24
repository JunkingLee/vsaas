#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> Maven package (JDK 21)"
mvn -B clean package -DskipTests

echo "==> Collect JARs -> release/jars"
rm -rf release/jars && mkdir -p release/jars
MODULES=(vsaas-auth vsaas-device vsaas-media vsaas-tenant vsaas-record vsaas-alarm vsaas-billing vsaas-user)
for m in "${MODULES[@]}"; do
  jar=$(ls "$m"/target/*.jar 2>/dev/null | grep -v original | head -1 || true)
  if [[ -z "${jar}" ]]; then
    echo "ERROR: missing jar for $m" >&2
    exit 1
  fi
  cp -f "$jar" "release/jars/${m}.jar"
  echo "  ${m}.jar"
done

echo "==> Frontend build -> release/web"
if [[ ! -d frontend/node_modules ]]; then
  (cd frontend && npm install)
fi
(cd frontend && npm run build)
rm -rf release/web && mkdir -p release/web
cp -a frontend/dist/. release/web/

echo "==> Done"
ls -lh release/jars
du -sh release/web
echo "Next: docker compose -f docker/docker-compose.release.yml up -d --build"
