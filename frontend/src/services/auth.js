const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

async function request(path, options = {}) {
  const { token, ...fetchOptions } = options
  const response = await fetch(`${API_URL}${path}`, {
    ...fetchOptions,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...fetchOptions.headers,
    },
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) {
    throw new Error(data.message || data.detail || 'Não foi possível conectar ao DevQuiz.')
  }
  return data
}

export function cadastrarUsuario(apelido, pin) {
  return request('/api/auth/cadastro', {
    method: 'POST',
    body: JSON.stringify({ apelido, pin }),
  })
}

export function entrarUsuario(apelido, pin) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ apelido, pin }),
  })
}

export function buscarPerfil(token) {
  return request('/api/usuarios/perfil', { token })
}

export function listarCategorias(token) {
  return request('/api/quiz/categorias', { token })
}

export function listarTentativasPendentes(token) {
  return request('/api/quiz/tentativas/pendentes', { token })
}

export function listarHistoricoTentativas(token) {
  return request('/api/quiz/tentativas/historico', { token })
}

export function iniciarTentativa(token, payload) {
  return request('/api/quiz/tentativas', {
    method: 'POST',
    token,
    body: JSON.stringify(payload),
  })
}

export function obterTentativa(token, tentativaId) {
  return request(`/api/quiz/tentativas/${tentativaId}`, { token })
}

export function salvarResposta(token, tentativaId, payload) {
  return request(`/api/quiz/tentativas/${tentativaId}/respostas`, {
    method: 'PUT',
    token,
    body: JSON.stringify(payload),
  })
}

export function finalizarTentativa(token, tentativaId) {
  return request(`/api/quiz/tentativas/${tentativaId}/finalizar`, {
    method: 'POST',
    token,
  })
}
