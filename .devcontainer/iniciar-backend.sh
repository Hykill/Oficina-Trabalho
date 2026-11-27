#!/usr/bin/env bash
# Sobe o Spring Boot no Codespace e deixa a porta 8080 pública
# para o site do GitHub Pages conseguir chamar a API.
cd "$(dirname "$0")/../backend"

echo "Esperando o MySQL..."
for i in $(seq 1 60); do
  (echo > /dev/tcp/127.0.0.1/3306) 2>/dev/null && break
  sleep 2
done

echo "Subindo o backend (log em /tmp/backend.log)..."
nohup bash mvnw spring-boot:run > /tmp/backend.log 2>&1 &

for i in $(seq 1 120); do
  curl -s -o /dev/null http://localhost:8080/clientes && break
  sleep 2
done

gh codespace ports visibility 8080:public -c "$CODESPACE_NAME"

echo "API no ar: https://${CODESPACE_NAME}-8080.${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN}"
