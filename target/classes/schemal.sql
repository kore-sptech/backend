CREATE TABLE IF NOT EXIST produto(
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(45) NOT NULL,
    descricao VARCHAR(80),
    possuiValidade TINYINT NOT NULL,
    qtdMinAlert INT NOT NULL
);
