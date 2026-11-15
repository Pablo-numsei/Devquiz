# DevQuiz

Aplicação web de quizzes para praticar fundamentos de desenvolvimento de software.

## Tecnologias

- React
- Vite
- JavaScript
- CSS responsivo
- LocalStorage para perfis locais, progresso e histórico
- Vercel para hospedagem

## Como executar localmente

1. Entre na pasta do frontend:
   `cd frontend`
2. Instale as dependências:
   `npm install`
3. Inicie o ambiente de desenvolvimento:
   `npm run dev`

Para validar a versão de produção:

`npm run build`

## Persistência dos dados

O DevQuiz utiliza o LocalStorage do navegador. Os perfis, desafios em andamento, respostas e resultados ficam salvos no próprio dispositivo e navegador utilizado.

Os dados não são sincronizados entre dispositivos ou navegadores. Limpar os dados do site ou o armazenamento do navegador pode apagar o progresso. O PIN é usado somente para separar perfis locais; não representa autenticação segura em servidor.

## Banco de questões

As categorias e perguntas ficam em `frontend/src/data/questions.js`. Cada questão possui categoria, dificuldade, enunciado, alternativas, resposta correta e explicação.

## Publicação na Vercel

Configure o projeto com:

- Root Directory: `frontend`
- Framework Preset: Vite
- Build Command: `npm run build`
- Output Directory: `dist`

Não é necessário configurar Render, servidor Java, conexão JDBC ou banco SQL para executar a versão publicada.

## Estrutura

- `frontend/`: aplicação React e catálogo local de questões.
- `backend/`: implementação Java/Spring Boot desenvolvida durante a etapa inicial do projeto; não é necessária para a versão final baseada em armazenamento local.
- `database/`: scripts SQL da implementação inicial; não são executados pela versão final.
