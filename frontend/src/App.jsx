import { useEffect, useState } from 'react'
import {
  ArrowRight,
  Braces,
  Check,
  ChevronRight,
  Code2,
  Database,
  Globe,
  Layers3,
  LockKeyhole,
  LogOut,
  Terminal,
  Trophy,
  Zap,
} from 'lucide-react'
import './App.css'
import { cadastrarUsuario, entrarUsuario, buscarPerfil, listarCategorias, listarTentativasPendentes, iniciarTentativa, obterTentativa, salvarResposta, finalizarTentativa } from './services/auth.js'

const categoryIcons = {
  JavaScript: Braces,
  React: Layers3,
  'HTML & CSS': Globe,
  Java: Code2,
  SQL: Database,
  Segurança: LockKeyhole,
}


function AuthScreen({ onAuthenticated }) {
  const [mode, setMode] = useState('login')
  const [apelido, setApelido] = useState('')
  const [pin, setPin] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      const response = mode === 'register'
        ? await cadastrarUsuario(apelido.trim(), pin)
        : await entrarUsuario(apelido.trim(), pin)
      localStorage.setItem('devquiz_session', JSON.stringify(response))
      onAuthenticated(response)
    } catch (err) {
      setError(err.message || 'Não foi possível realizar o acesso.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-shell">
      <section className="auth-panel">
        <a className="brand auth-brand" href="#inicio">
          <span className="brand-mark"><Code2 size={20} /></span>
          <span>dev<span className="brand-accent">quiz</span></span>
        </a>
        <div className="auth-heading">
          <div className="eyebrow"><span className="eyebrow-line" /> SUA JORNADA COMEÇA AQUI</div>
          <h1>{mode === 'register' ? 'Crie seu perfil.' : 'Entre para praticar.'}</h1>
          <p>{mode === 'register' ? 'Escolha um apelido e um PIN de 4 dígitos para salvar seu progresso.' : 'Acesse seu perfil e continue acompanhando sua evolução.'}</p>
        </div>
        <div className="auth-tabs">
          <button type="button" className={mode === 'login' ? 'auth-tab active' : 'auth-tab'} onClick={() => { setMode('login'); setError('') }}>Entrar</button>
          <button type="button" className={mode === 'register' ? 'auth-tab active' : 'auth-tab'} onClick={() => { setMode('register'); setError('') }}>Criar perfil</button>
        </div>
        <form className="auth-form" onSubmit={submit}>
          <label htmlFor="nickname">APELIDO</label>
          <input id="nickname" value={apelido} onChange={(event) => setApelido(event.target.value.replace(/[^a-zA-Z0-9_]/g, '').slice(0, 20))} placeholder="Ex.: DevPablo" minLength={3} maxLength={20} autoComplete="username" required />
          <small>De 3 a 20 caracteres. Use letras, números ou _.</small>
          <label htmlFor="pin">PIN DE 4 DÍGITOS</label>
          <input id="pin" type="password" inputMode="numeric" pattern="[0-9]{4}" maxLength={4} value={pin} onChange={(event) => setPin(event.target.value.replace(/\D/g, '').slice(0, 4))} placeholder="••••" autoComplete={mode === 'register' ? 'new-password' : 'current-password'} required />
          {error && <div className="auth-error" role="alert">{error}</div>}
          <button className="primary-button auth-submit" type="submit" disabled={loading || apelido.trim().length < 3 || pin.length !== 4}>
            {loading ? 'Aguarde...' : mode === 'register' ? 'Criar perfil' : 'Entrar no DevQuiz'} <ArrowRight size={17} />
          </button>
        </form>
        <p className="auth-privacy"><LockKeyhole size={13} /> Seu PIN é armazenado de forma protegida.</p>
      </section>
      <aside className="auth-aside">
        <div className="auth-aside-glow" />
        <div className="auth-code-card">
          <div className="window-top"><div className="window-dots"><i /><i /><i /></div><span>seu_progresso.js</span><Terminal size={15} /></div>
          <div className="code-content">
            <div className="code-line"><span className="line-number">01</span><span><b className="code-purple">const</b> <b className="code-blue">progresso</b> = {'{'}</span></div>
            <div className="code-line"><span className="line-number">02</span><span className="indent"><b className="code-cyan">pratica</b>: <b className="code-green">"constante"</b>,</span></div>
            <div className="code-line"><span className="line-number">03</span><span className="indent"><b className="code-cyan">desafios</b>: <b className="code-orange">"ilimitados"</b>,</span></div>
            <div className="code-line"><span className="line-number">04</span><span className="indent"><b className="code-cyan">evolucao</b>: <b className="code-green">true</b></span></div>
            <div className="code-line"><span className="line-number">05</span><span>{'}'}</span></div>
          </div>
          <div className="window-bottom"><span><span className="live-dot" /> DevQuiz</span><span>READY</span></div>
        </div>
        <p>Aprenda no seu ritmo. Registre cada conquista.</p>
      </aside>
    </main>
  )
}

function App() {
  const [session, setSession] = useState(null)
  const [categoryOptions, setCategoryOptions] = useState([])
  const [selectedCategoryIds, setSelectedCategoryIds] = useState([])
  const [difficulty, setDifficulty] = useState('Iniciante')
  const [started, setStarted] = useState(false)
  const [quiz, setQuiz] = useState(null)
  const [questionIndex, setQuestionIndex] = useState(0)
  const [selected, setSelected] = useState(null)
  const [feedback, setFeedback] = useState(null)
  const [quizResult, setQuizResult] = useState(null)
  const [pendingAttempts, setPendingAttempts] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const saved = localStorage.getItem('devquiz_session')
    if (!saved) return
    try {
      const parsed = JSON.parse(saved)
      if (!parsed.token) throw new Error('Sessão inválida')
      buscarPerfil(parsed.token)
        .then((profile) => setSession({ ...parsed, usuario: profile }))
        .catch(() => localStorage.removeItem('devquiz_session'))
    } catch {
      localStorage.removeItem('devquiz_session')
    }
  }, [])

  useEffect(() => {
    if (!session?.token) return
    let active = true
    Promise.all([
      listarCategorias(session.token),
      listarTentativasPendentes(session.token),
    ])
      .then(([categorias, pendentes]) => {
        if (!active) return
        setCategoryOptions(categorias)
        setPendingAttempts(pendentes)
      })
      .catch((err) => {
        if (active) setError(err.message || 'Não foi possível carregar os dados do DevQuiz.')
      })
    return () => { active = false }
  }, [session?.token])

  const refreshPendingAttempts = async () => {
    if (!session?.token) return
    try {
      setPendingAttempts(await listarTentativasPendentes(session.token))
    } catch (err) {
      setError(err.message)
    }
  }

  const handleAuthenticated = (data) => setSession(data)

  const toggleCategory = (id) => {
    setError('')
    setSelectedCategoryIds((current) => current.includes(id)
      ? current.filter((item) => item !== id)
      : [...current, id])
  }

  const startQuiz = async () => {
    if (selectedCategoryIds.length === 0) {
      setError('Selecione pelo menos uma categoria.')
      return
    }
    setLoading(true)
    setError('')
    try {
      const data = await iniciarTentativa(session.token, {
        categoriaIds: selectedCategoryIds,
        dificuldade: difficulty,
        quantidade: 10,
      })
      setQuiz(data)
      setQuestionIndex(0)
      setSelected(null)
      setFeedback(null)
      setQuizResult(null)
      setStarted(true)
      await refreshPendingAttempts()
    } catch (err) {
      setError(err.message || 'Não foi possível iniciar o desafio.')
    } finally {
      setLoading(false)
    }
  }

  const resumeAttempt = async (attemptId) => {
    setLoading(true)
    setError('')
    try {
      const data = await obterTentativa(session.token, attemptId)
      setQuiz(data)
      const firstUnanswered = data.questoes.findIndex((question) => !question.alternativaSelecionadaId)
      if (firstUnanswered === -1) {
        const result = await finalizarTentativa(session.token, attemptId)
        setQuizResult(result)
        setQuestionIndex(data.questoes.length - 1)
      } else {
        setQuestionIndex(firstUnanswered)
      }
      setSelected(null)
      setFeedback(null)
      setStarted(true)
    } catch (err) {
      setError(err.message || 'Não foi possível retomar o desafio.')
      await refreshPendingAttempts()
    } finally {
      setLoading(false)
    }
  }

  const currentQuestion = quiz?.questoes?.[questionIndex]

  const confirmAnswer = async () => {
    if (!currentQuestion || selected === null) return
    setLoading(true)
    setError('')
    try {
      const response = await salvarResposta(session.token, quiz.tentativaId, {
        questaoId: currentQuestion.id,
        alternativaId: selected,
      })
      setFeedback(response)
      await refreshPendingAttempts()
    } catch (err) {
      setError(err.message || 'Não foi possível salvar sua resposta.')
    } finally {
      setLoading(false)
    }
  }

  const nextQuestion = async () => {
    if (!quiz || !feedback) return
    if (questionIndex < quiz.questoes.length - 1) {
      setQuestionIndex((index) => index + 1)
      setSelected(null)
      setFeedback(null)
      return
    }

    setLoading(true)
    setError('')
    try {
      const result = await finalizarTentativa(session.token, quiz.tentativaId)
      setQuizResult(result)
      await refreshPendingAttempts()
    } catch (err) {
      setError(err.message || 'Não foi possível finalizar o desafio.')
    } finally {
      setLoading(false)
    }
  }

  const resetQuiz = async () => {
    setStarted(false)
    setQuiz(null)
    setQuestionIndex(0)
    setSelected(null)
    setFeedback(null)
    setQuizResult(null)
    setError('')
    await refreshPendingAttempts()
  }

  const logout = () => {
    localStorage.removeItem('devquiz_session')
    setSession(null)
    setCategoryOptions([])
    setSelectedCategoryIds([])
    setPendingAttempts([])
    setStarted(false)
    setQuiz(null)
    setQuizResult(null)
    setError('')
  }

  if (!session) return <AuthScreen onAuthenticated={handleAuthenticated} />

  if (started && quiz) {
    const totalQuestions = quiz.questoes.length
    const progress = quizResult ? 100 : ((questionIndex + (feedback ? 1 : 0)) / totalQuestions) * 100

    return (
      <main className="quiz-page">
        <header className="topbar">
          <button className="brand brand-button" onClick={resetQuiz} aria-label="Salvar e sair do desafio">
            <span className="brand-mark"><Code2 size={20} /></span>
            <span>dev<span className="brand-accent">quiz</span></span>
          </button>
          <span className="quiz-mode"><span className="live-dot" /> {quizResult ? 'Resultado' : 'Modo prática'}</span>
        </header>
        <section className="question-wrap">
          <button className="back-link" onClick={resetQuiz}>← Salvar e voltar ao início</button>

          {quizResult ? (
            <article className="question-card result-card">
              <p className="question-kicker">DESAFIO CONCLUÍDO</p>
              <h1>Seu resultado</h1>
              <div className="hero-stats result-stats">
                <div><strong>{quizResult.acertos}/{quizResult.totalQuestoes}</strong><span>ACERTOS</span></div>
                <i />
                <div><strong>{quizResult.pontuacao}</strong><span>PONTOS</span></div>
              </div>
              <p className="result-summary">Cada resposta foi corrigida pelo servidor e seu resultado foi salvo.</p>
              <div className="result-details">
                {quizResult.detalhes.map((detail, index) => (
                  <div className="result-detail" key={detail.questaoId}>
                    <strong>Questão {index + 1}</strong>
                    <span className={detail.correta ? 'result-correct' : 'result-incorrect'}>
                      {detail.correta ? 'Correta' : 'Incorreta'}
                    </span>
                    {!detail.correta && detail.textoAlternativaCorreta && (
                      <p>Resposta correta: {detail.textoAlternativaCorreta}</p>
                    )}
                    <p>{detail.explicacao}</p>
                  </div>
                ))}
              </div>
              <div className="question-actions">
                <button className="primary-button" onClick={resetQuiz}>Voltar ao início <ArrowRight size={17} /></button>
              </div>
            </article>
          ) : currentQuestion ? (
            <>
              <div className="question-meta">
                <span className="eyebrow">{currentQuestion.categoria} <span className="meta-separator">/</span> {quiz.dificuldade}</span>
                <span className="question-number">QUESTÃO {String(questionIndex + 1).padStart(2, '0')} <span>DE {String(totalQuestions).padStart(2, '0')}</span></span>
              </div>
              <div className="progress-track"><span style={{ width: `${progress}%` }} /></div>
              <article className="question-card">
                <p className="question-kicker">CONCEITO FUNDAMENTAL</p>
                <h1>{currentQuestion.enunciado}</h1>
                {currentQuestion.codigo && <pre className="question-code"><code>{currentQuestion.codigo}</code></pre>}
                <div className="answers-list">
                  {currentQuestion.alternativas.map((alternative, index) => {
                    const isSelected = selected === alternative.id
                    const isCorrect = feedback?.alternativaCorretaId === alternative.id
                    const isIncorrect = feedback && isSelected && !feedback.correta
                    return (
                      <button
                        className={`answer-option ${isSelected ? 'selected' : ''} ${feedback && isCorrect ? 'correct' : ''} ${isIncorrect ? 'incorrect' : ''}`}
                        key={alternative.id}
                        onClick={() => !feedback && setSelected(alternative.id)}
                        disabled={Boolean(feedback) || loading}
                      >
                        <span className="answer-letter">{String.fromCharCode(65 + index)}</span>
                        <span>{alternative.texto}</span>
                        {feedback && isCorrect && <Check size={18} className="answer-check" />}
                      </button>
                    )
                  })}
                </div>
                {feedback && (
                  <div className="explanation">
                    <strong>{feedback.correta ? 'Resposta correta.' : 'Resposta incorreta.'}</strong>
                    {feedback.textoAlternativaCorreta && !feedback.correta && <p>Resposta correta: {feedback.textoAlternativaCorreta}</p>}
                    <p>{feedback.explicacao}</p>
                  </div>
                )}
                {error && <div className="auth-error" role="alert">{error}</div>}
                <div className="question-actions">
                  {feedback ? (
                    <button className="primary-button" onClick={nextQuestion} disabled={loading}>
                      {loading ? 'Salvando...' : questionIndex === totalQuestions - 1 ? 'Ver resultado' : 'Próxima questão'} <ArrowRight size={17} />
                    </button>
                  ) : (
                    <button className="primary-button" disabled={selected === null || loading} onClick={confirmAnswer}>
                      {loading ? 'Salvando resposta...' : 'Confirmar resposta'} <ArrowRight size={17} />
                    </button>
                  )}
                </div>
              </article>
              <p className="question-footnote">Sua resposta é salva no banco ao confirmar. Você pode sair e continuar depois.</p>
            </>
          ) : (
            <div className="question-card"><p>Não foi possível carregar as questões desta tentativa.</p></div>
          )}
        </section>
      </main>
    )
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="#inicio" aria-label="DevQuiz início">
          <span className="brand-mark"><Code2 size={20} /></span>
          <span>dev<span className="brand-accent">quiz</span></span>
        </a>
        <nav className="main-nav">
          <a className="nav-active" href="#inicio">Início</a>
          <a href="#categorias">Categorias</a>
          <a href="#sobre">Sobre</a>
        </nav>
        <div className="user-session"><span className="user-avatar">{session.usuario?.apelido?.slice(0, 1).toUpperCase()}</span><span className="user-name">{session.usuario?.apelido}</span><button className="logout-button" onClick={logout} aria-label="Sair"><LogOut size={15} /></button></div>
      </header>

      <section className="hero-section" id="inicio">
        <div className="hero-copy">
          <div className="eyebrow"><span className="eyebrow-line" /> APRENDA. PRATIQUE. EVOLUA.</div>
          <h1>Seu próximo nível<br />começa com um <span>desafio.</span></h1>
          <p className="hero-description">Teste seus conhecimentos em programação, descubra onde melhorar e acompanhe sua evolução, uma questão de cada vez.</p>
          <div className="hero-actions">
            <a className="primary-button" href="#categorias">Começar agora <ArrowRight size={17} /></a>
            <span className="hero-note"><Zap size={15} /> Aprenda no seu ritmo</span>
          </div>
          <div className="hero-stats">
            <div><strong>{String(categoryOptions.length).padStart(2, '0')}</strong><span>ÁREAS DE ESTUDO</span></div>
            <i />
            <div><strong>100<span>+</span></strong><span>QUESTÕES PLANEJADAS</span></div>
            <i />
            <div><strong>03</strong><span>NÍVEIS</span></div>
          </div>
        </div>

        <div className="hero-visual" aria-label="Ilustração de código">
          <div className="visual-glow" />
          <div className="code-window">
            <div className="window-top">
              <div className="window-dots"><i /><i /><i /></div>
              <span>developer.js</span>
              <Terminal size={15} />
            </div>
            <div className="code-content">
              <div className="code-line"><span className="line-number">01</span><span><b className="code-purple">const</b> <b className="code-blue">developer</b> = {'{'}</span></div>
              <div className="code-line"><span className="line-number">02</span><span className="indent"><b className="code-cyan">curiosity</b>: <b className="code-green">true</b>,</span></div>
              <div className="code-line"><span className="line-number">03</span><span className="indent"><b className="code-cyan">practice</b>: <b className="code-green">"daily"</b>,</span></div>
              <div className="code-line"><span className="line-number">04</span><span className="indent"><b className="code-cyan">level</b>: <b className="code-orange">"up"</b>,</span></div>
              <div className="code-line"><span className="line-number">05</span><span>{'}'}</span></div>
              <div className="code-line code-spacer"><span className="line-number">06</span><span /></div>
              <div className="code-line"><span className="line-number">07</span><span><b className="code-purple">function</b> <b className="code-blue">evolve</b>() {'{'}</span></div>
              <div className="code-line"><span className="line-number">08</span><span className="indent"><b className="code-purple">return</b> developer.practice;</span></div>
              <div className="code-line"><span className="line-number">09</span><span>{'}'}</span></div>
            </div>
            <div className="window-bottom"><span><span className="live-dot" /> JavaScript</span><span>UTF-8</span><span>Ln 09, Col 1</span></div>
          </div>
          <div className="floating-card"><span className="floating-icon"><Trophy size={17} /></span><div><strong>Seu progresso</strong><small>Começa aqui</small></div><ChevronRight size={16} /></div>
          <div className="visual-orbit orbit-one" />
          <div className="visual-orbit orbit-two" />
        </div>
      </section>

      {pendingAttempts.length > 0 && (
        <section className="pending-section">
          <div className="section-heading">
            <div><div className="eyebrow"><span className="eyebrow-line" /> CONTINUE DE ONDE PAROU</div><h2>Desafios <span>em andamento</span></h2></div>
            <p>Seu progresso está salvo no banco de dados.</p>
          </div>
          <div className="pending-grid">
            {pendingAttempts.map((attempt) => (
              <article className="pending-card" key={attempt.tentativaId}>
                <div>
                  <strong>{attempt.categorias.join(' + ')}</strong>
                  <small>{attempt.dificuldade} · {attempt.respondidas} de {attempt.totalQuestoes} respondidas</small>
                </div>
                <button className="primary-button" onClick={() => resumeAttempt(attempt.tentativaId)} disabled={loading}>
                  Continuar <ArrowRight size={16} />
                </button>
              </article>
            ))}
          </div>
        </section>
      )}

      <section className="categories-section" id="categorias">
        <div className="section-heading">
          <div><div className="eyebrow"><span className="eyebrow-line" /> ESCOLHA SEU FOCO</div><h2>O que vamos <span>praticar?</span></h2></div>
          <p>Selecione uma ou mais áreas para<br />combinar seus desafios.</p>
        </div>
        <div className="category-grid">
          {categoryOptions.map((category) => {
            const Icon = categoryIcons[category.nome] || Code2
            const isSelected = selectedCategoryIds.includes(category.id)
            return (
              <button key={category.id} className={`category-card ${isSelected ? 'category-selected' : ''}`} onClick={() => toggleCategory(category.id)} aria-pressed={isSelected}>
                <span className="category-icon"><Icon size={20} /></span>
                <span className="category-info"><strong>{category.nome}</strong><small>{category.descricao}</small><em>{isSelected ? 'Selecionada' : 'Clique para selecionar'}</em></span>
                <span className="category-check">{isSelected ? <Check size={14} /> : <ChevronRight size={16} />}</span>
              </button>
            )
          })}
        </div>
        <div className="quiz-config">
          <div className="difficulty-block"><span className="config-label">NÍVEL DE DIFICULDADE</span><div className="difficulty-options">
            {['Iniciante', 'Intermediário', 'Avançado'].map((level) => <button key={level} className={difficulty === level ? 'difficulty-active' : ''} onClick={() => setDifficulty(level)}>{level}</button>)}
          </div></div>
          <button className="primary-button start-button" onClick={startQuiz} disabled={loading || selectedCategoryIds.length === 0}>
            {loading ? 'Preparando...' : 'Iniciar desafio'} <ArrowRight size={17} />
          </button>
        </div>
        {error && <div className="auth-error app-error" role="alert">{error}</div>}
        {categoryOptions.length === 0 && !error && <p className="question-footnote">Carregando categorias...</p>}
      </section>

      <footer className="site-footer" id="sobre"><a className="brand footer-brand" href="#inicio"><span className="brand-mark"><Code2 size={18} /></span><span>dev<span className="brand-accent">quiz</span></span></a><span>Feito para quem transforma curiosidade em código.</span><span className="footer-version">DEVQUIZ <b>·</b> 2026</span></footer>
    </main>
  )
}

export default App
