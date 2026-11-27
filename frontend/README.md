# Oficina — Front-end (Vue 3 + Vite)

Painel administrativo da oficina mecânica, ligado ao back-end Spring Boot da pasta `backend/`.

## Como rodar

Pré-requisitos: **Node.js 18 ou mais novo** (https://nodejs.org) e o back-end rodando em `http://localhost:8080`.

```bash
cd frontend
npm install      # baixa as dependências (só na primeira vez)
npm run dev      # sobe em http://localhost:5173
```

Build de produção: `npm run build` (gera a pasta `dist/`).

## Por que não dá erro de CORS?

O Spring Boot **não tem configuração de CORS**. Em vez de mexer no back-end, o `vite.config.js` tem um **proxy**:

```
navegador → http://localhost:5173/api/clientes  →  (Vite repassa)  →  http://localhost:8080/clientes
```

Para o navegador, tudo vem da mesma origem (`localhost:5173`), então ele não bloqueia nada.
Se um dia o front for servido separado do back-end, aí será preciso adicionar `@CrossOrigin` ou um `WebMvcConfigurer` no Spring.

## Estrutura

```
src/
├── main.js                 # cria a aplicação Vue e registra o router
├── App.vue                 # layout: Sidebar + área de conteúdo
├── assets/main.css         # tema escuro (variáveis CSS) e estilos reutilizáveis
├── router/index.js         # rotas: /, /clientes, /veiculos, /configuracoes
├── services/
│   ├── api.js              # instância do Axios + tradução das mensagens de erro do Spring
│   ├── clienteService.js   # CRUD de /clientes
│   └── carroService.js     # CRUD de /carros
├── composables/
│   ├── useToast.js         # notificações (sucesso / erro)
│   └── useSettings.js      # preferências salvas no localStorage
├── utils/
│   ├── formatters.js       # máscaras de CPF, telefone e placa
│   └── validators.js       # validação dos formulários
├── components/             # Sidebar, Modal, Confirmação, Toasts, Paginação, StatCard, BarChart
└── views/                  # uma página por rota
```

## Decisões tomadas a partir do back-end

| Situação no back-end | Como o front lida |
|---|---|
| Sem endpoint de busca | A pesquisa (nome/CPF, placa/modelo/marca) é feita no front sobre a lista de `GET`. |
| Sem data de cadastro nas entidades | "Últimos adicionados" = maiores IDs (AUTO_INCREMENT). Está avisado no Dashboard. |
| Banco guarda só dígitos (`12345678900`) | O front exibe com máscara, mas envia só os dígitos. |
| CPF/placa duplicados retornam **409** | A mensagem do Spring aparece no próprio campo e num toast. |
| Excluir cliente com carro / carro com serviço retorna **409** | A mensagem do Spring é exibida no toast. |
| Sem endpoint `/clientes/{id}/carros` | Os veículos do cliente são filtrados de `GET /carros` por `clienteId`. |
| Itens de serviço não têm rota por serviço | A ordem de serviço filtra `GET /itens-servico` por `servicoId`. Lançar a mesma peça de novo soma a quantidade. |
| Não existe campo "total" no serviço | Total = mão de obra + Σ(preço da peça × quantidade), calculado no front. |
| `pecaPreco` vem do cadastro atual da peça | Se o preço da peça mudar, o total de serviços antigos muda junto (avisado na tela). |
| Excluir serviço com itens / peça em uso retorna **409** | A mensagem do Spring aparece no toast; o diálogo de exclusão já avisa quando o serviço tem peças. |

O CPF é validado só por quantidade de dígitos (11), sem checar os dígitos verificadores, porque os dados
de exemplo do Postman (`12345678900`) não passariam nessa checagem.
