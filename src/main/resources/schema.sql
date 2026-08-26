CREATE TABLE IF NOT EXISTS categoria (
    id_categoria BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(45) NOT NULL,
    descricao VARCHAR(150),
    fk_usuario BIGINT,
    PRIMARY KEY (id_categoria)
);

ALTER TABLE categoria
    ADD CONSTRAINT fk_categoria_usuario
    FOREIGN KEY (fk_usuario) REFERENCES usuario(id_usuario);

ALTER TABLE produto
    ADD COLUMN fk_categoria INT,
 ADD CONSTRAINT fk_categoria FOREIGN KEY (fk_categoria) REFERENCES categoria(id_categoria);
