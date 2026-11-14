-- Primeiro lote de questões de JavaScript para iniciantes.
-- Execute no banco DevQuiz, depois de 02_quiz_schema.sql.
-- Pode executar novamente: questões e alternativas não serão duplicadas.

SET NOCOUNT ON;

DECLARE @categoriaId INT;
DECLARE @questaoId BIGINT;

SELECT @categoriaId = id
FROM dbo.categorias
WHERE nome = 'JavaScript' AND ativa = 1;

IF @categoriaId IS NULL
    THROW 50001, 'Categoria JavaScript não encontrada ou inativa.', 1;

-- Questão 1
IF NOT EXISTS (
    SELECT 1 FROM dbo.questoes
    WHERE categoria_id = @categoriaId
      AND dificuldade = 'Iniciante'
      AND enunciado = N'O que o método map() retorna ao ser usado em um array?'
)
BEGIN
    INSERT INTO dbo.questoes (categoria_id, dificuldade, enunciado, codigo, explicacao)
    VALUES (
        @categoriaId,
        'Iniciante',
        N'O que o método map() retorna ao ser usado em um array?',
        N'const numeros = [1, 2, 3];
const dobrados = numeros.map(numero => numero * 2);',
        N'map() percorre o array e cria um novo array com o resultado da função aplicada a cada elemento. O array original não é alterado.'
    );
END;

SELECT @questaoId = id
FROM dbo.questoes
WHERE categoria_id = @categoriaId
  AND dificuldade = 'Iniciante'
  AND enunciado = N'O que o método map() retorna ao ser usado em um array?';

IF (SELECT COUNT(*) FROM dbo.alternativas WHERE questao_id = @questaoId) <> 4
BEGIN
    DELETE FROM dbo.alternativas WHERE questao_id = @questaoId;

    INSERT INTO dbo.alternativas (questao_id, texto, correta)
    VALUES
        (@questaoId, N'O mesmo array, modificado diretamente', 0),
        (@questaoId, N'Um novo array com os valores transformados', 1),
        (@questaoId, N'Um valor booleano', 0),
        (@questaoId, N'A quantidade de elementos do array', 0);
END;

-- Questão 2
IF NOT EXISTS (
    SELECT 1 FROM dbo.questoes
    WHERE categoria_id = @categoriaId
      AND dificuldade = 'Iniciante'
      AND enunciado = N'Qual operador compara valor e tipo ao mesmo tempo em JavaScript?'
)
BEGIN
    INSERT INTO dbo.questoes (categoria_id, dificuldade, enunciado, codigo, explicacao)
    VALUES (
        @categoriaId,
        'Iniciante',
        N'Qual operador compara valor e tipo ao mesmo tempo em JavaScript?',
        N'10 === ''10''',
        N'O operador === faz comparação estrita: os valores precisam ser iguais e também ter o mesmo tipo. Por isso, 10 === ''10'' resulta em false.'
    );
END;

SELECT @questaoId = id
FROM dbo.questoes
WHERE categoria_id = @categoriaId
  AND dificuldade = 'Iniciante'
  AND enunciado = N'Qual operador compara valor e tipo ao mesmo tempo em JavaScript?';

IF (SELECT COUNT(*) FROM dbo.alternativas WHERE questao_id = @questaoId) <> 4
BEGIN
    DELETE FROM dbo.alternativas WHERE questao_id = @questaoId;

    INSERT INTO dbo.alternativas (questao_id, texto, correta)
    VALUES
        (@questaoId, N'=', 0),
        (@questaoId, N'==', 0),
        (@questaoId, N'===', 1),
        (@questaoId, N'!=', 0);
END;

-- Questão 3
IF NOT EXISTS (
    SELECT 1 FROM dbo.questoes
    WHERE categoria_id = @categoriaId
      AND dificuldade = 'Iniciante'
      AND enunciado = N'Qual declaração permite reatribuir um valor à variável?'
)
BEGIN
    INSERT INTO dbo.questoes (categoria_id, dificuldade, enunciado, codigo, explicacao)
    VALUES (
        @categoriaId,
        'Iniciante',
        N'Qual declaração permite reatribuir um valor à variável?',
        N'let pontuacao = 10;
pontuacao = 20;',
        N'Variáveis declaradas com let podem receber novos valores. const impede a reatribuição da variável.'
    );
END;

SELECT @questaoId = id
FROM dbo.questoes
WHERE categoria_id = @categoriaId
  AND dificuldade = 'Iniciante'
  AND enunciado = N'Qual declaração permite reatribuir um valor à variável?';

IF (SELECT COUNT(*) FROM dbo.alternativas WHERE questao_id = @questaoId) <> 4
BEGIN
    DELETE FROM dbo.alternativas WHERE questao_id = @questaoId;

    INSERT INTO dbo.alternativas (questao_id, texto, correta)
    VALUES
        (@questaoId, N'const', 0),
        (@questaoId, N'let', 1),
        (@questaoId, N'function', 0),
        (@questaoId, N'class', 0);
END;

-- Questão 4
IF NOT EXISTS (
    SELECT 1 FROM dbo.questoes
    WHERE categoria_id = @categoriaId
      AND dificuldade = 'Iniciante'
      AND enunciado = N'O que a palavra-chave return faz dentro de uma função?'
)
BEGIN
    INSERT INTO dbo.questoes (categoria_id, dificuldade, enunciado, codigo, explicacao)
    VALUES (
        @categoriaId,
        'Iniciante',
        N'O que a palavra-chave return faz dentro de uma função?',
        N'function somar(a, b) {
  return a + b;
}',
        N'return encerra a execução da função e devolve o valor indicado para quem chamou a função.'
    );
END;

SELECT @questaoId = id
FROM dbo.questoes
WHERE categoria_id = @categoriaId
  AND dificuldade = 'Iniciante'
  AND enunciado = N'O que a palavra-chave return faz dentro de uma função?';

IF (SELECT COUNT(*) FROM dbo.alternativas WHERE questao_id = @questaoId) <> 4
BEGIN
    DELETE FROM dbo.alternativas WHERE questao_id = @questaoId;

    INSERT INTO dbo.alternativas (questao_id, texto, correta)
    VALUES
        (@questaoId, N'Repete a função automaticamente', 0),
        (@questaoId, N'Devolve um valor e encerra a função', 1),
        (@questaoId, N'Declara uma variável global', 0),
        (@questaoId, N'Imprime o resultado no console', 0);
END;

-- Questão 5
IF NOT EXISTS (
    SELECT 1 FROM dbo.questoes
    WHERE categoria_id = @categoriaId
      AND dificuldade = 'Iniciante'
      AND enunciado = N'Qual método seleciona os elementos de um array que atendem a uma condição?'
)
BEGIN
    INSERT INTO dbo.questoes (categoria_id, dificuldade, enunciado, codigo, explicacao)
    VALUES (
        @categoriaId,
        'Iniciante',
        N'Qual método seleciona os elementos de um array que atendem a uma condição?',
        N'const pares = numeros.filter(numero => numero % 2 === 0);',
        N'filter() cria um novo array contendo somente os elementos para os quais a função de teste retorna true.'
    );
END;

SELECT @questaoId = id
FROM dbo.questoes
WHERE categoria_id = @categoriaId
  AND dificuldade = 'Iniciante'
  AND enunciado = N'Qual método seleciona os elementos de um array que atendem a uma condição?';

IF (SELECT COUNT(*) FROM dbo.alternativas WHERE questao_id = @questaoId) <> 4
BEGIN
    DELETE FROM dbo.alternativas WHERE questao_id = @questaoId;

    INSERT INTO dbo.alternativas (questao_id, texto, correta)
    VALUES
        (@questaoId, N'map()', 0),
        (@questaoId, N'push()', 0),
        (@questaoId, N'filter()', 1),
        (@questaoId, N'join()', 0);
END;

-- Questão 6
IF NOT EXISTS (
    SELECT 1 FROM dbo.questoes
    WHERE categoria_id = @categoriaId
      AND dificuldade = 'Iniciante'
      AND enunciado = N'Qual é o resultado de typeof ''DevQuiz''?'
)
BEGIN
    INSERT INTO dbo.questoes (categoria_id, dificuldade, enunciado, codigo, explicacao)
    VALUES (
        @categoriaId,
        'Iniciante',
        N'Qual é o resultado de typeof ''DevQuiz''?',
        N'typeof ''DevQuiz''',
        N'typeof retorna uma string que identifica o tipo do operando. Textos entre aspas possuem o tipo string.'
    );
END;

SELECT @questaoId = id
FROM dbo.questoes
WHERE categoria_id = @categoriaId
  AND dificuldade = 'Iniciante'
  AND enunciado = N'Qual é o resultado de typeof ''DevQuiz''?';

IF (SELECT COUNT(*) FROM dbo.alternativas WHERE questao_id = @questaoId) <> 4
BEGIN
    DELETE FROM dbo.alternativas WHERE questao_id = @questaoId;

    INSERT INTO dbo.alternativas (questao_id, texto, correta)
    VALUES
        (@questaoId, N'text', 0),
        (@questaoId, N'string', 1),
        (@questaoId, N'object', 0),
        (@questaoId, N'boolean', 0);
END;

-- Questão 7
IF NOT EXISTS (
    SELECT 1 FROM dbo.questoes
    WHERE categoria_id = @categoriaId
      AND dificuldade = 'Iniciante'
      AND enunciado = N'Quantas vezes o bloco abaixo será executado?'
)
BEGIN
    INSERT INTO dbo.questoes (categoria_id, dificuldade, enunciado, codigo, explicacao)
    VALUES (
        @categoriaId,
        'Iniciante',
        N'Quantas vezes o bloco abaixo será executado?',
        N'for (let i = 0; i < 3; i++) {
  console.log(i);
}',
        N'O laço começa em 0 e continua enquanto i for menor que 3. Os valores são 0, 1 e 2, totalizando três execuções.'
    );
END;

SELECT @questaoId = id
FROM dbo.questoes
WHERE categoria_id = @categoriaId
  AND dificuldade = 'Iniciante'
  AND enunciado = N'Quantas vezes o bloco abaixo será executado?';

IF (SELECT COUNT(*) FROM dbo.alternativas WHERE questao_id = @questaoId) <> 4
BEGIN
    DELETE FROM dbo.alternativas WHERE questao_id = @questaoId;

    INSERT INTO dbo.alternativas (questao_id, texto, correta)
    VALUES
        (@questaoId, N'Duas vezes', 0),
        (@questaoId, N'Três vezes', 1),
        (@questaoId, N'Quatro vezes', 0),
        (@questaoId, N'Infinitamente', 0);
END;

-- Questão 8
IF NOT EXISTS (
    SELECT 1 FROM dbo.questoes
    WHERE categoria_id = @categoriaId
      AND dificuldade = 'Iniciante'
      AND enunciado = N'Qual sintaxe permite interpolar uma variável em uma string usando template literals?'
)
BEGIN
    INSERT INTO dbo.questoes (categoria_id, dificuldade, enunciado, codigo, explicacao)
    VALUES (
        @categoriaId,
        'Iniciante',
        N'Qual sintaxe permite interpolar uma variável em uma string usando template literals?',
        N'const nome = ''Pablo'';
const mensagem = `Olá, ${nome}!`;',
        N'Template literals utilizam crases e permitem inserir expressões dentro de ${...}.'
    );
END;

SELECT @questaoId = id
FROM dbo.questoes
WHERE categoria_id = @categoriaId
  AND dificuldade = 'Iniciante'
  AND enunciado = N'Qual sintaxe permite interpolar uma variável em uma string usando template literals?';

IF (SELECT COUNT(*) FROM dbo.alternativas WHERE questao_id = @questaoId) <> 4
BEGIN
    DELETE FROM dbo.alternativas WHERE questao_id = @questaoId;

    INSERT INTO dbo.alternativas (questao_id, texto, correta)
    VALUES
        (@questaoId, N'''Olá, '' + {nome}', 0),
        (@questaoId, N'"Olá, %nome%"', 0),
        (@questaoId, N'`Olá, ${nome}!`', 1),
        (@questaoId, N'''Olá, ${nome}!''', 0);
END;

-- Questão 9
IF NOT EXISTS (
    SELECT 1 FROM dbo.questoes
    WHERE categoria_id = @categoriaId
      AND dificuldade = 'Iniciante'
      AND enunciado = N'Qual será o resultado da expressão !true?'
)
BEGIN
    INSERT INTO dbo.questoes (categoria_id, dificuldade, enunciado, codigo, explicacao)
    VALUES (
        @categoriaId,
        'Iniciante',
        N'Qual será o resultado da expressão !true?',
        N'!true',
        N'O operador lógico ! nega o valor booleano. Portanto, !true resulta em false.'
    );
END;

SELECT @questaoId = id
FROM dbo.questoes
WHERE categoria_id = @categoriaId
  AND dificuldade = 'Iniciante'
  AND enunciado = N'Qual será o resultado da expressão !true?';

IF (SELECT COUNT(*) FROM dbo.alternativas WHERE questao_id = @questaoId) <> 4
BEGIN
    DELETE FROM dbo.alternativas WHERE questao_id = @questaoId;

    INSERT INTO dbo.alternativas (questao_id, texto, correta)
    VALUES
        (@questaoId, N'true', 0),
        (@questaoId, N'false', 1),
        (@questaoId, N'null', 0),
        (@questaoId, N'undefined', 0);
END;

-- Questão 10
IF NOT EXISTS (
    SELECT 1 FROM dbo.questoes
    WHERE categoria_id = @categoriaId
      AND dificuldade = 'Iniciante'
      AND enunciado = N'Qual método adiciona um elemento ao final de um array?'
)
BEGIN
    INSERT INTO dbo.questoes (categoria_id, dificuldade, enunciado, codigo, explicacao)
    VALUES (
        @categoriaId,
        'Iniciante',
        N'Qual método adiciona um elemento ao final de um array?',
        N'const frutas = [''maçã'', ''banana''];
frutas.push(''uva'');',
        N'push() adiciona um ou mais elementos ao final do array e retorna o novo tamanho do array.'
    );
END;

SELECT @questaoId = id
FROM dbo.questoes
WHERE categoria_id = @categoriaId
  AND dificuldade = 'Iniciante'
  AND enunciado = N'Qual método adiciona um elemento ao final de um array?';

IF (SELECT COUNT(*) FROM dbo.alternativas WHERE questao_id = @questaoId) <> 4
BEGIN
    DELETE FROM dbo.alternativas WHERE questao_id = @questaoId;

    INSERT INTO dbo.alternativas (questao_id, texto, correta)
    VALUES
        (@questaoId, N'pop()', 0),
        (@questaoId, N'shift()', 0),
        (@questaoId, N'unshift()', 0),
        (@questaoId, N'push()', 1);
END;

