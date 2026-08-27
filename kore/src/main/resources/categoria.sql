CREATE TABLE IF NOT EXISTS categoria (
                                         id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         nome VARCHAR (45) NOT NULL,
    descricao VARCHAR (150)
    );

ALTER TABLE produto
    ADD COLUMN fk_categoria INT,
 ADD CONSTRAINT fk_categoria FOREIGN KEY (fk_categoria) REFERENCES Categoria(id_categoria);

ALTER TABLE `Categoria`
    ADD COLUMN fk_usuario BIGINT,
  ADD CONSTRAINT fk_usuario FOREIGN KEY (fk_usuario) REFERENCES usuario(id_usuario);