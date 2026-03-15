USE test;

-- Tabela Produto
CREATE TABLE IF NOT EXISTS Produto (
    id_produto INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(45) NOT NULL,
    descricao VARCHAR(45) NOT NULL,
    possui_validade VARCHAR(45) NOT NULL,
    qtd_min_alerta VARCHAR(45) NOT NULL
);

-- Tabela Cliente
CREATE TABLE IF NOT EXISTS Cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(45) NOT NULL,
    contato VARCHAR(45) NOT NULL
);

-- Tabela Orcamento
CREATE TABLE IF NOT EXISTS Orcamento (
    id_orcamento INT AUTO_INCREMENT PRIMARY KEY,
    fk_cliente INT,
    valor_orcado VARCHAR(45) NOT NULL,
    FOREIGN KEY (fk_cliente) REFERENCES Cliente(id_cliente)
);

-- Tabela Sessao
CREATE TABLE IF NOT EXISTS Sessao (
    id_sessao INT AUTO_INCREMENT PRIMARY KEY,
    fk_orcamento INT,
    valor_pago VARCHAR(45) NOT NULL,
    dt_sessao VARCHAR(45) NOT NULL,
    hr_comeco VARCHAR(45) NOT NULL,
    hr_termino VARCHAR(45) NOT NULL,
    meio_pagamento VARCHAR(45) NOT NULL,
    FOREIGN KEY (fk_orcamento) REFERENCES Orcamento(id_orcamento)
);

-- Tabela Item
CREATE TABLE IF NOT EXISTS Item (
    id_item INT AUTO_INCREMENT PRIMARY KEY,
    dt_Entrada VARCHAR(45) NOT NULL,
    se_ativo VARCHAR(45) NOT NULL,
    dt_saida VARCHAR(45) NOT NULL,
    fk_produto INT,
    fk_sessao INT,
    FOREIGN KEY (fk_produto) REFERENCES Produto(id_produto),
    FOREIGN KEY (fk_sessao) REFERENCES Sessao(id_sessao)
);

-- Tabela Usuario
CREATE TABLE IF NOT EXISTS Usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(45) NOT NULL,
    nome VARCHAR(45) NOT NULL,
    senha VARCHAR(45) NOT NULL
);

-- Tabela Transacao
CREATE TABLE IF NOT EXISTS Transacao (
    id_transacoes INT AUTO_INCREMENT PRIMARY KEY,
    valor VARCHAR(45) NOT NULL,
    nome VARCHAR(45) NOT NULL,
    tipo VARCHAR(45) NOT NULL,
    categoria VARCHAR(45) NOT NULL,
    dataCriacao VARCHAR(45) NOT NULL,
    fk_item INT,
    fk_item_produto INT,
    fk_sessao INT,
    FOREIGN KEY (fk_item) REFERENCES Item(id_item),
    FOREIGN KEY (fk_item_produto) REFERENCES Produto(id_produto),
    FOREIGN KEY (fk_sessao) REFERENCES Sessao(id_sessao)
);
