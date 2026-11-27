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

## Site publicado (modo demonstração)

Todo push na `main` que altera `frontend/` publica o painel em
https://hykill.github.io/Oficina-Trabalho/

O GitHub Pages só hospeda arquivos estáticos e não roda o Spring Boot. Por isso o site
publicado é gerado com `VITE_MODO_DEMO=true`: as chamadas à API são respondidas dentro do
próprio navegador por [frontend/src/services/demoApi.js](frontend/src/services/demoApi.js),
que repete as regras, mensagens e códigos HTTP dos Services do backend.

- Abre direto, sem precisar ligar nenhum servidor.
- Vem com dados de exemplo; o que for cadastrado fica salvo só no navegador de quem usa.
- Em **Configurações → Restaurar dados de exemplo** os dados voltam ao início.

Rodando local (`npm run dev`), o front usa o backend Java de verdade.
