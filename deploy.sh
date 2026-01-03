#!/usr/bin/env bash
set -euo pipefail

: "${APP_IMAGE:?APP_IMAGE is required}"

cd /opt/GestionIncidents

echo "Pull repo..."
git pull

echo "Deploying image: $APP_IMAGE"
export APP_IMAGE="$APP_IMAGE"

docker compose -f docker-compose.deploy.yml pull app
docker compose -f docker-compose.deploy.yml up -d --no-deps app

echo "Done."
