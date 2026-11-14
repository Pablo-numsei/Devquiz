import { useState } from 'react'
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
  Terminal,
  Trophy,
  Zap,
} from 'lucide-react'
import './App.css'

const categories = [
  { name: 'JavaScript', description: 'Fundamentos, funções e lógica', icon: Braces, count: '24 questões' },
  { name: 'React', description: 'Componentes, hooks e estado', icon: Layers3, count: '18 questões' },
  { name: 'HTML & CSS', description: 'Estrutura, estilo e responsividade', icon: Globe, count: '20 questões' },
  { name: 'Java', description: 'POO, sintaxe e coleções', icon: Code2, count: '22 questões' },
  { name: 'SQL', description: 'Consultas, relacionamentos e dados', icon: Database, count: '16 questões' },
  { name: 'Segurança', description: 'Web, redes e boas práticas', icon: LockKeyhole, count: '14 questões' },
]

const sampleQuestion = {
  category: 'JavaScript',
  question: 'Qual método transforma um array em uma única string, unindo seus elementos?',
  code: "const tecnologias = ['HTML', 'CSS', 'JS']",
  answers: ['map()', 'filter()', 'join()', 'reduce()'],
  correct: 2,
  explanation: 'O método join() une todos os elementos de um array em uma string. É possível definir o separador entre eles.',
}

function App() {
  const [category, setCategory] = useState('JavaScript')
  const [difficulty, setDifficulty] = useState('Iniciante')
  const [started, setStarted] = useState(false)
  const [selected, setSelected] = useState(null)
  const [finished, setFinished] = useState(false)

  const startQuiz = () => {
    setSelected(null)
    setFinished(false)
    setStarted(true)
  }

  const resetQuiz = () => {
    setStarted(false)
    setSelected(null)
    setFinished(false)
  }

  if (started) {
    return (
      <main className="quiz-page">
        <header className="topbar">
          <button className="brand brand-button" onClick={resetQuiz} aria-label="Voltar ao início">
            <span className="brand-mark"><Code2 size={20} /></span>
            <span>dev<span className="brand-accent">quiz</span></span>
          </button>
          <span className="quiz-mode"><span className="live-dot" /> Modo prática</span>
        </header>
        <section className="question-wrap">
          <button className="back-link" onClick={resetQuiz}>← Voltar ao início</button>
          <div className="question-meta">
            <span className="eyebrow">{category} <span className="meta-separator">/</span> {difficulty}</span>
            <span className="question-number">QUESTÃO 01 <span>DE 10</span></span>
          </div>
          <div className="progress-track"><span /></div>
          <article className="question-card">
            <p className="question-kicker">CONCEITO FUNDAMENTAL</p>
            <h1>{sampleQuestion.question}</h1>
            <pre className="question-code"><code>{sampleQuestion.code}</code></pre>
            <div className="answers-list">
              {sampleQuestion.answers.map((answer, index) => {
                const isSelected = selected === index
                const isCorrect = index === sampleQuestion.correct
                const showResult = finished
                return (
                  <button
                    className={`answer-option ${isSelected ? 'selected' : ''} ${showResult && isCorrect ? 'correct' : ''} ${showResult && isSelected && !isCorrect ? 'incorrect' : ''}`}
                    key={answer}
                    onClick={() => !finished && setSelected(index)}
                    disabled={finished}
                  >
                    <span className="answer-letter">{String.fromCharCode(65 + index)}</span>
                    <span>{answer}</span>
                    {showResult && isCorrect && <Check size={18} className="answer-check" />}
                  </button>
                )
              })}
            </div>
            {finished && (
              <div className="explanation">
                <strong>{selected === sampleQuestion.correct ? 'Resposta correta.' : 'Não foi dessa vez.'}</strong>
                <p>{sampleQuestion.explanation}</p>
              </div>
            )}
            <div className="question-actions">
              {finished ? (
                <button className="primary-button" onClick={resetQuiz}>Voltar ao início <ArrowRight size={17} /></button>
              ) : (
                <button className="primary-button" disabled={selected === null} onClick={() => setFinished(true)}>
                  Confirmar resposta <ArrowRight size={17} />
                </button>
              )}
            </div>
          </article>
          <p className="question-footnote">Seu progresso fica salvo nesta sessão.</p>
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
        <div className="header-status"><span className="live-dot" /> Plataforma de estudos</div>
      </header>

      <section className="hero-section" id="inicio">
        <div className="hero-copy">
          <div className="eyebrow"><span className="eyebrow-line" /> APRENDA. PRATIQUE. EVOLUA.</div>
          <h1>Seu próximo nível<br />começa com um <span>desafio.</span></h1>
          <p className="hero-description">Teste seus conhecimentos em programação, descubra onde melhorar e acompanhe sua evolução — uma questão de cada vez.</p>
          <div className="hero-actions">
            <a className="primary-button" href="#categorias">Começar agora <ArrowRight size={17} /></a>
            <span className="hero-note"><Zap size={15} /> Aprenda no seu ritmo</span>
          </div>
          <div className="hero-stats">
            <div><strong>06</strong><span>ÁREAS DE ESTUDO</span></div>
            <i />
            <div><strong>100<span>+</span></strong><span>QUESTÕES</span></div>
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

      <section className="categories-section" id="categorias">
        <div className="section-heading">
          <div><div className="eyebrow"><span className="eyebrow-line" /> ESCOLHA SEU FOCO</div><h2>O que vamos <span>praticar?</span></h2></div>
          <p>Selecione uma área para começar<br />a testar seus conhecimentos.</p>
        </div>
        <div className="category-grid">
          {categories.map(({ name, description, icon: Icon, count }) => (
            <button key={name} className={`category-card ${category === name ? 'category-selected' : ''}`} onClick={() => setCategory(name)}>
              <span className="category-icon"><Icon size={20} /></span>
              <span className="category-info"><strong>{name}</strong><small>{description}</small><em>{count}</em></span>
              <span className="category-check">{category === name ? <Check size={14} /> : <ChevronRight size={16} />}</span>
            </button>
          ))}
        </div>
        <div className="quiz-config">
          <div className="difficulty-block"><span className="config-label">NÍVEL DE DIFICULDADE</span><div className="difficulty-options">
            {['Iniciante', 'Intermediário', 'Avançado'].map((level) => <button key={level} className={difficulty === level ? 'difficulty-active' : ''} onClick={() => setDifficulty(level)}>{level}</button>)}
          </div></div>
          <button className="primary-button start-button" onClick={startQuiz}>Iniciar desafio <ArrowRight size={17} /></button>
        </div>
      </section>

      <footer className="site-footer" id="sobre"><a className="brand footer-brand" href="#inicio"><span className="brand-mark"><Code2 size={18} /></span><span>dev<span className="brand-accent">quiz</span></span></a><span>Feito para quem transforma curiosidade em código.</span><span className="footer-version">DEVQUIZ <b>·</b> 2026</span></footer>
    </main>
  )
}

export default App
