const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

async function request(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
      ...options.headers,
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
