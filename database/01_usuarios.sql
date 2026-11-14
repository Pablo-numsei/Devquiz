-- Execute este script no banco SQL Server do DevQuiz.
IF OBJECT_ID('dbo.usuarios', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.usuarios (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        apelido VARCHAR(20) NOT NULL,
        pin_hash VARCHAR(100) NOT NULL,
        criado_em DATETIME2 NOT NULL CONSTRAINT DF_usuarios_criado_em DEFAULT SYSUTCDATETIME(),
        CONSTRAINT UQ_usuarios_apelido UNIQUE (apelido)
    );
END;
