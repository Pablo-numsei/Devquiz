import { categories, questions } from '../data/questions.js'

const DATA_KEY = 'devquiz_local_data_v1'
const SESSION_KEY = 'devquiz_session'

function readData() {
  try {
    return JSON.parse(localStorage.getItem(DATA_KEY)) || { users: [], attempts: [], nextAttemptId: 1 }
  } catch {
    return { users: [], attempts: [], nextAttemptId: 1 }
  }
}

function writeData(data) {
  localStorage.setItem(DATA_KEY, JSON.stringify(data))
}

function createId() {
  return globalThis.crypto?.randomUUID?.() || `local-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

async function hashPin(pin) {
  if (globalThis.crypto?.subtle) {
    const bytes = new TextEncoder().encode(pin)
    const digest = await crypto.subtle.digest('SHA-256', bytes)
    return Array.from(new Uint8Array(digest), (byte) => byte.toString(16).padStart(2, '0')).join('')
  }
  return btoa(pin)
}

function currentUser(token) {
  const session = JSON.parse(localStorage.getItem(SESSION_KEY) || 'null')
  if (!session || session.token !== token) throw new Error('Sua sessão local expirou. Entre novamente.')
  const user = readData().users.find((item) => item.apelido === session.usuario.apelido)
  if (!user) throw new Error('Perfil local não encontrado.')
  return user
}

export async function cadastrarUsuario(apelido, pin) {
  const normalized = apelido.trim()
  if (!/^[A-Za-z0-9_]{3,20}$/.test(normalized)) throw new Error('Use um apelido de 3 a 20 caracteres.')
  if (!/^\d{4}$/.test(pin)) throw new Error('O PIN deve conter 4 dígitos.')
  const data = readData()
  if (data.users.some((user) => user.apelido.toLowerCase() === normalized.toLowerCase())) {
    throw new Error('Este apelido já está cadastrado neste navegador.')
  }
  const user = { apelido: normalized, pinHash: await hashPin(pin), criadoEm: new Date().toISOString() }
  data.users.push(user)
  writeData(data)
  const response = { token: createId(), usuario: { apelido: user.apelido, criadoEm: user.criadoEm } }
  localStorage.setItem(SESSION_KEY, JSON.stringify(response))
  return response
}

export async function entrarUsuario(apelido, pin) {
  const user = readData().users.find((item) => item.apelido.toLowerCase() === apelido.trim().toLowerCase())
  if (!user || user.pinHash !== await hashPin(pin)) throw new Error('Apelido ou PIN incorreto.')
  const response = { token: createId(), usuario: { apelido: user.apelido, criadoEm: user.criadoEm } }
  localStorage.setItem(SESSION_KEY, JSON.stringify(response))
  return response
}

export async function buscarPerfil(token) {
  const user = currentUser(token)
  return { apelido: user.apelido, criadoEm: user.criadoEm }
}

export async function listarCategorias() {
  return categories
}

export async function listarTentativasPendentes(token) {
  const user = currentUser(token)
  return readData().attempts
    .filter((attempt) => attempt.apelido === user.apelido && attempt.status === 'EM_ANDAMENTO')
    .sort((a, b) => new Date(b.criadoEm) - new Date(a.criadoEm))
    .map((attempt) => ({
      tentativaId: attempt.tentativaId,
      dificuldade: attempt.dificuldade,
      totalQuestoes: attempt.questoes.length,
      respondidas: Object.keys(attempt.respostas).length,
      categorias: attempt.categorias,
      criadoEm: attempt.criadoEm,
    }))
}

export async function listarHistoricoTentativas(token) {
  const user = currentUser(token)
  return readData().attempts
    .filter((attempt) => attempt.apelido === user.apelido && attempt.status === 'FINALIZADA')
    .sort((a, b) => new Date(b.criadoEm) - new Date(a.criadoEm))
    .map((attempt) => ({
      tentativaId: attempt.tentativaId,
      categorias: attempt.categorias,
      dificuldade: attempt.dificuldade,
      totalQuestoes: attempt.questoes.length,
      acertos: attempt.acertos,
      pontuacao: attempt.pontuacao,
      criadoEm: attempt.criadoEm,
    }))
}

function getAttempt(token, attemptId) {
  const user = currentUser(token)
  const data = readData()
  const attempt = data.attempts.find((item) => item.tentativaId === Number(attemptId) && item.apelido === user.apelido)
  if (!attempt) throw new Error('Desafio não encontrado neste navegador.')
  return { data, attempt }
}

function toQuizResponse(attempt) {
  return {
    tentativaId: attempt.tentativaId,
    dificuldade: attempt.dificuldade,
    questoes: attempt.questoes.map((question) => ({
      id: question.id,
      categoria: categories.find((category) => category.id === question.categoriaId)?.nome || 'Geral',
      enunciado: question.enunciado,
      codigo: question.codigo,
      alternativas: question.alternativas.map(({ id, texto }) => ({ id, texto })),
      alternativaSelecionadaId: attempt.respostas[question.id]?.alternativaId ?? null,
    })),
  }
}

export async function iniciarTentativa(token, payload) {
  const user = currentUser(token)
  const selectedIds = [...new Set(payload.categoriaIds || [])].map(Number)
  if (!selectedIds.length) throw new Error('Selecione pelo menos uma categoria.')
  const selectedQuestions = questions.filter((question) =>
    selectedIds.includes(question.categoriaId) && question.dificuldade === payload.dificuldade
  )
  if (selectedQuestions.length < 10) {
    throw new Error('Ainda não existem 10 questões disponíveis para essa combinação. Selecione outras categorias ou outro nível.')
  }
  const shuffled = [...selectedQuestions]
  for (let index = shuffled.length - 1; index > 0; index -= 1) {
    const randomIndex = Math.floor(Math.random() * (index + 1))
    ;[shuffled[index], shuffled[randomIndex]] = [shuffled[randomIndex], shuffled[index]]
  }
  const chosen = shuffled.slice(0, 10)
  const data = readData()
  const attempt = {
    tentativaId: data.nextAttemptId++,
    apelido: user.apelido,
    categorias: categories.filter((category) => selectedIds.includes(category.id)).map((category) => category.nome),
    dificuldade: payload.dificuldade,
    questoes: chosen,
    respostas: {},
    status: 'EM_ANDAMENTO',
    criadoEm: new Date().toISOString(),
  }
  data.attempts.push(attempt)
  writeData(data)
  return toQuizResponse(attempt)
}

export async function obterTentativa(token, attemptId) {
  const { attempt } = getAttempt(token, attemptId)
  if (attempt.status !== 'EM_ANDAMENTO') throw new Error('Este desafio já foi finalizado.')
  return toQuizResponse(attempt)
}

export async function salvarResposta(token, attemptId, payload) {
  const { data, attempt } = getAttempt(token, attemptId)
  if (attempt.status !== 'EM_ANDAMENTO') throw new Error('Este desafio já foi finalizado.')
  const question = attempt.questoes.find((item) => item.id === Number(payload.questaoId))
  if (!question) throw new Error('Questão não pertence a este desafio.')
  const alternative = question.alternativas.find((item) => item.id === Number(payload.alternativaId))
  if (!alternative) throw new Error('Alternativa inválida.')
  attempt.respostas[question.id] = { alternativaId: alternative.id, correta: alternative.correta }
  writeData(data)
  const correct = question.alternativas.find((item) => item.correta)
  return {
    questaoId: question.id,
    correta: alternative.correta,
    alternativaSelecionadaId: alternative.id,
    alternativaCorretaId: correct?.id ?? null,
    textoAlternativaCorreta: correct?.texto ?? null,
    explicacao: question.explicacao,
  }
}

export async function finalizarTentativa(token, attemptId) {
  const { data, attempt } = getAttempt(token, attemptId)
  if (attempt.status === 'FINALIZADA') throw new Error('Este desafio já foi finalizado.')
  const acertos = attempt.questoes.filter((question) => attempt.respostas[question.id]?.correta).length
  const pontosPorAcerto = attempt.dificuldade === 'Iniciante' ? 10 : attempt.dificuldade === 'Intermediário' ? 20 : 30
  const detalhes = attempt.questoes.map((question) => {
    const answer = attempt.respostas[question.id]
    const correct = question.alternativas.find((item) => item.correta)
    return {
      questaoId: question.id,
      alternativaSelecionadaId: answer?.alternativaId ?? null,
      correta: Boolean(answer?.correta),
      alternativaCorretaId: correct?.id ?? null,
      textoAlternativaCorreta: correct?.texto ?? null,
      explicacao: question.explicacao,
    }
  })
  attempt.status = 'FINALIZADA'
  attempt.acertos = acertos
  attempt.pontuacao = acertos * pontosPorAcerto
  attempt.finalizadoEm = new Date().toISOString()
  writeData(data)
  return {
    tentativaId: attempt.tentativaId,
    totalQuestoes: attempt.questoes.length,
    acertos,
    pontuacao: attempt.pontuacao,
    pontosPorAcerto,
    detalhes,
  }
}

export async function revisarTentativa(token, attemptId) {
  const { attempt } = getAttempt(token, attemptId)
  if (attempt.status !== 'FINALIZADA') throw new Error('Somente desafios concluídos podem ser revisados.')
  return {
    tentativaId: attempt.tentativaId,
    categorias: attempt.categorias,
    dificuldade: attempt.dificuldade,
    totalQuestoes: attempt.questoes.length,
    acertos: attempt.acertos,
    pontuacao: attempt.pontuacao,
    criadoEm: attempt.criadoEm,
    questoes: attempt.questoes.map((question) => {
      const answer = attempt.respostas[question.id]
      return {
        questaoId: question.id,
        categoria: categories.find((category) => category.id === question.categoriaId)?.nome || 'Geral',
        enunciado: question.enunciado,
        codigo: question.codigo,
        alternativaSelecionadaId: answer?.alternativaId ?? null,
        alternativaCorretaId: question.alternativas.find((item) => item.correta)?.id ?? null,
        respostaCorreta: Boolean(answer?.correta),
        alternativas: question.alternativas.map((alternative) => ({
          id: alternative.id,
          texto: alternative.texto,
          selecionada: answer?.alternativaId === alternative.id,
          correta: alternative.correta,
        })),
        explicacao: question.explicacao,
      }
    }),
  }
}
