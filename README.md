# Oficina-Trabalho

Sistema de gestão de oficina mecânica, organizado como monorepo.

```
Oficina-Trabalho/
├── backend/     # API REST em Spring Boot (Java 17 + Maven)
├── frontend/    # Painel administrativo em Vue 3 + Vite
└── .github/
    └── workflows/
        └── deploy-pages.yml   # publica o frontend no GitHub Pages
```

## Como rodar

### Backend

```bash
cd backend
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

O painel sobe em `http://localhost:5173` e repassa as chamadas `/api/*` para o backend.
Mais detalhes em [frontend/README.md](frontend/README.md).

## Backend no GitHub Codespaces

O repositório já vem configurado (pasta `.devcontainer/`): ao abrir um Codespace,
o MySQL e o backend sobem sozinhos e a porta 8080 fica pública.

1. No GitHub: **Code → Codespaces → Create codespace on main**.
2. Espere uns 2–3 minutos. O endereço da API aparece na aba **Ports** (porta 8080)
   e em `/tmp/iniciar-backend.log`, no formato
   `https://<nome-do-codespace>-8080.app.github.dev`.
3. O deploy já aponta para o Codespace **friendly goldfish**. Se criar outro Codespace,
   troque o endereço em `.github/workflows/deploy-pages.yml` (ou crie a variável
   `API_URL` em **Settings → Secrets and variables → Actions → Variables**) e rode de novo
   o workflow **Deploy do front no GitHub Pages** (aba Actions → Run workflow).

O Codespace desliga sozinho depois de um tempo sem uso. Para usar o site de novo,
basta religá-lo em **Code → Codespaces**: o endereço e os dados do banco continuam os mesmos.

## Deploy

Todo push na `main` que altera `frontend/` publica o painel em
https://hykill.github.io/Oficina-Trabalho/
