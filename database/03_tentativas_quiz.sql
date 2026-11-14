-- Controle das perguntas pertencentes a cada tentativa.
-- Execute depois de 02_quiz_schema.sql no banco DevQuiz.

IF COL_LENGTH('dbo.tentativas', 'status') IS NULL
BEGIN
    ALTER TABLE dbo.tentativas
    ADD status VARCHAR(20) NOT NULL
        CONSTRAINT DF_tentativas_status DEFAULT 'EM_ANDAMENTO';
END;

IF OBJECT_ID('dbo.tentativa_questoes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tentativa_questoes (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        tentativa_id BIGINT NOT NULL,
        questao_id BIGINT NOT NULL,
        CONSTRAINT FK_tentativa_questoes_tentativas
            FOREIGN KEY (tentativa_id) REFERENCES dbo.tentativas(id),
        CONSTRAINT FK_tentativa_questoes_questoes
            FOREIGN KEY (questao_id) REFERENCES dbo.questoes(id),
        CONSTRAINT UQ_tentativa_questoes UNIQUE (tentativa_id, questao_id)
    );
END;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_tentativa_questoes_tentativa'
      AND object_id = OBJECT_ID('dbo.tentativa_questoes')
)
    CREATE INDEX IX_tentativa_questoes_tentativa
        ON dbo.tentativa_questoes (tentativa_id);
