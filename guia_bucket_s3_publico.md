# Guia Prático: Criando e Configurando um Bucket S3 Público na AWS (VocLabs)

Este guia prático detalha o processo de criação de um bucket no Amazon S3 e a configuração necessária para torná-lo público, permitindo o acesso direto às fotos enviadas através de URLs públicas.

---

## Pré-requisitos
* Acesso ao console da AWS (neste cenário, utilizando o ambiente de laboratório **VocLabs / AWS Academy**).
* Conta com permissões básicas para gerenciamento de buckets S3.

---

## Passo 1: Criar o Bucket no Amazon S3

1. Faça login no console da AWS e procure pelo serviço **S3**.
2. No painel principal do S3, clique no botão **Create bucket** (Criar bucket).
3. Preencha as configurações gerais:
   * **Bucket name:** Escolha um nome exclusivo globalmente (ex: `meu-app-fotos-bucket`).
   * **AWS Region:** Selecione a região desejada (ex: `us-east-1` - Leste dos EUA).
4. **Configurações de Acesso Público (Importante):**
   * Por padrão, a AWS bloqueia todo o acesso público. Desmarque a opção **Block *all* public access** temporariamente para permitir a exposição pública posterior.
   * Confirme a caixa de aviso que aparecerá na tela reconhecendo que o bucket ficará público.
5. Deixe as demais configurações padrão e clique em **Create bucket** no final da página.

---

## Passo 2: Desativar o Block Public Access (Caso não tenha feito na criação)

Se o bucket já foi criado e o acesso público ainda está restrito:

1. Acesse a lista de buckets e clique no **nome do seu bucket**.
2. Vá para a aba **Permissions** (Permissões).
3. Na seção **Block public access (bucket settings)**, clique em **Edit** (Editar).
4. Desmarque a opção **Block *all* public access**.
5. Clique em **Save changes** (Salvar alterações) e digite `confirm` para confirmar.

> **Nota sobre o erro de Access Analyzer:** Caso utilize uma conta de laboratório (VocLabs) e receba um aviso informando que o seu usuário não está autorizado a realizar a ação `access-analyzer:ValidatePolicy`, **ignore o erro**. Trata-se de uma limitação das permissões temporárias do laboratório para validar políticas via console, mas isso **não impede** que você salve as alterações.

---

## Passo 3: Adicionar a Bucket Policy para Leitura Pública

Para que qualquer pessoa consiga visualizar as imagens hospedadas, é necessário adicionar uma política de permissão de leitura (`GetObject`).

1. Ainda na aba **Permissions** do seu bucket, role até a seção **Bucket policy** (Política de bucket) e clique em **Edit** (Editar).
2. Cole o seguinte JSON de política, substituindo `SEU-BUCKET-NAME` pelo **nome exato** do seu bucket:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::SEU-BUCKET-NAME/*"
        }
    ]
}
```

3. Clique em **Save changes** (Salvar alterações).

---

## Passo 4: Testando o Acesso às Fotos

Com as configurações aplicadas, qualquer foto enviada para o bucket poderá ser acessada publicamente através do navegador ou da sua aplicação utilizando a URL padrão do objeto:

* **Formato padrão:** `https://SEU-BUCKET-NAME.s3.amazonaws.com/nome-da-pasta/foto.jpg`
* **Formato com região (ex: us-east-1):** `https://SEU-BUCKET-NAME.s3.us-east-1.amazonaws.com/nome-da-pasta/foto.jpg`

---

## Dica de Segurança
Como o bucket está configurado com leitura pública global (`Principal: "*"`), qualquer arquivo enviado para ele ficará acessível imediatamente na internet. Caso precise gerenciar arquivos privados no futuro, considere utilizar URLs assinadas (*Presigned URLs*) geradas diretamente pelo seu backend.
