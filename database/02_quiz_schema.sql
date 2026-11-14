-- Estrutura relacional do banco DevQuiz.
-- Execute depois de 01_usuarios.sql no banco DevQuiz.

IF OBJECT_ID('dbo.categorias', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.categorias (
        id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        nome VARCHAR(60) NOT NULL,
        descricao NVARCHAR(160) NOT NULL,
        ativa BIT NOT NULL CONSTRAINT DF_categorias_ativa DEFAULT 1,
        CONSTRAINT UQ_categorias_nome UNIQUE (nome)
    );
END;

IF OBJECT_ID('dbo.questoes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.questoes (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        categoria_id INT NOT NULL,
        dificuldade VARCHAR(20) NOT NULL,
        enunciado NVARCHAR(1000) NOT NULL,
        codigo NVARCHAR(MAX) NULL,
        explicacao NVARCHAR(MAX) NOT NULL,
        ativa BIT NOT NULL CONSTRAINT DF_questoes_ativa DEFAULT 1,
        criado_em DATETIME2 NOT NULL CONSTRAINT DF_questoes_criado_em DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_questoes_categorias FOREIGN KEY (categoria_id) REFERENCES dbo.categorias(id),
        CONSTRAINT CK_questoes_dificuldade CHECK (dificuldade IN ('Iniciante', 'Intermediário', 'Avançado'))
    );
END;

IF OBJECT_ID('dbo.alternativas', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.alternativas (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        questao_id BIGINT NOT NULL,
        texto NVARCHAR(500) NOT NULL,
        correta BIT NOT NULL CONSTRAINT DF_alternativas_correta DEFAULT 0,
        CONSTRAINT FK_alternativas_questoes FOREIGN KEY (questao_id) REFERENCES dbo.questoes(id)
    );
END;

IF OBJECT_ID('dbo.tentativas', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tentativas (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        usuario_id BIGINT NOT NULL,
        dificuldade VARCHAR(20) NOT NULL,
        total_questoes SMALLINT NOT NULL,
        acertos SMALLINT NOT NULL,
        pontuacao INT NOT NULL,
        criado_em DATETIME2 NOT NULL CONSTRAINT DF_tentativas_criado_em DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_tentativas_usuarios FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id),
        CONSTRAINT CK_tentativas_dificuldade CHECK (dificuldade IN ('Iniciante', 'Intermediário', 'Avançado')),
        CONSTRAINT CK_tentativas_total CHECK (total_questoes BETWEEN 1 AND 10),
        CONSTRAINT CK_tentativas_acertos CHECK (acertos BETWEEN 0 AND total_questoes)
    );
END;

IF OBJECT_ID('dbo.respostas', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.respostas (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        tentativa_id BIGINT NOT NULL,
        questao_id BIGINT NOT NULL,
        alternativa_id BIGINT NULL,
        correta BIT NOT NULL,
        CONSTRAINT FK_respostas_tentativas FOREIGN KEY (tentativa_id) REFERENCES dbo.tentativas(id),
        CONSTRAINT FK_respostas_questoes FOREIGN KEY (questao_id) REFERENCES dbo.questoes(id),
        CONSTRAINT FK_respostas_alternativas FOREIGN KEY (alternativa_id) REFERENCES dbo.alternativas(id),
        CONSTRAINT UQ_respostas_tentativa_questao UNIQUE (tentativa_id, questao_id)
    );
END;

IF NOT EXISTS (SELECT 1 FROM dbo.categorias WHERE nome = 'JavaScript')
    INSERT INTO dbo.categorias (nome, descricao) VALUES ('JavaScript', N'Fundamentos, funções e lógica');
IF NOT EXISTS (SELECT 1 FROM dbo.categorias WHERE nome = 'React')
    INSERT INTO dbo.categorias (nome, descricao) VALUES ('React', N'Componentes, hooks e estado');
IF NOT EXISTS (SELECT 1 FROM dbo.categorias WHERE nome = 'HTML & CSS')
    INSERT INTO dbo.categorias (nome, descricao) VALUES ('HTML & CSS', N'Estrutura, estilo e responsividade');
IF NOT EXISTS (SELECT 1 FROM dbo.categorias WHERE nome = 'Java')
    INSERT INTO dbo.categorias (nome, descricao) VALUES ('Java', N'POO, sintaxe e coleções');
IF NOT EXISTS (SELECT 1 FROM dbo.categorias WHERE nome = 'SQL')
    INSERT INTO dbo.categorias (nome, descricao) VALUES ('SQL', N'Consultas, relacionamentos e dados');
IF NOT EXISTS (SELECT 1 FROM dbo.categorias WHERE nome = 'Segurança')
    INSERT INTO dbo.categorias (nome, descricao) VALUES ('Segurança', N'Web, redes e boas práticas');

CREATE INDEX IX_questoes_categoria_dificuldade ON dbo.questoes (categoria_id, dificuldade, ativa);
CREATE INDEX IX_alternativas_questao ON dbo.alternativas (questao_id);
CREATE INDEX IX_tentativas_usuario_data ON dbo.tentativas (usuario_id, criado_em DESC);
CREATE INDEX IX_respostas_tentativa ON dbo.respostas (tentativa_id);
