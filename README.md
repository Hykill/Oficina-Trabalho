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

## Deploy

Todo push na `main` que altera `frontend/` publica o painel em
https://hykill.github.io/Oficina-Trabalho/
