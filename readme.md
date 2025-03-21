# FiscalSystem Backend

Este é o backend do FiscalSystem, uma aplicação destinada ao cálculo de impostos e à geração de relatórios (em PDF) para diversos cenários, como honorários, RRA/FEPA e IR para Pessoa Jurídica. A aplicação foi desenvolvida com Java (Spring Boot), JPA/Hibernate, e utiliza o PostgreSQL como banco de dados.

## Funcionalidades

- **Cálculos Fiscais:**
    - Cálculo de Imposto de Renda para honorários.
    - Cálculo de RRA + FEPA.
    - Cálculo de IR para Pessoa Jurídica (PJ).

- **Geração de Relatórios:**
    - Relatórios em PDF para cada tipo de cálculo (honorários, FEPA, RRA e PJ).
    - Assinatura digital dos PDFs gerados.

- **Autenticação:**
    - Cadastro e login de usuários com token JWT.
    - Endpoints protegidos (exceto os públicos, como o Swagger e o login).

- **Documentação da API:**
    - Swagger UI disponível para consulta dos endpoints.

## Tecnologias Utilizadas

- **Linguagem:** Java 17
- **Framework:** Spring Boot 3
- **Persistência:** Spring Data JPA / Hibernate
- **Banco de Dados:** PostgreSQL
- **Segurança:** Spring Security com JWT
- **Gerador de PDF:** PDFBox
- **Build e Deploy:** Docker, Docker Compose e Render

## Links Úteis

- **GitHub:** [https://github.com/LucasFelip/fiscalSystem-Backend](https://github.com/LucasFelip/fiscalSystem-Backend)
- **Documentação (Swagger):** [https://fiscalsystem-backend.onrender.com/swagger-ui/index.html](https://fiscalsystem-backend.onrender.com/swagger-ui/index.html)

## Configuração do Ambiente

O projeto utiliza variáveis de ambiente para configurar conexões com o banco de dados, JWT, e a porta do servidor. Essas variáveis são definidas em um arquivo `.env` que NÃO deve ser incluído no repositório.

### Exemplo de Variáveis de Ambiente

```properties
# Banco de Dados
DB_URL=jdbc:postgresql://<host>:5432/<database>
DB_USERNAME=<username>
DB_PASSWORD=<password>

# JWT
JWT_SECRET=<secret_key>
JWT_EXPIRATION_MS=3600000

# Porta do Servidor
SERVER_PORT=8080
```

> **Atenção:** Nunca compartilhe dados sensíveis. As informações acima devem ser configuradas conforme o ambiente (local, Render, etc).

## Como Executar o Projeto

### Utilizando Docker Compose

Certifique-se de ter o Docker e o Docker Compose instalados. No diretório raiz do projeto, utilize o seguinte comando:

```bash
docker-compose up --build
```

Isso iniciará dois containers: um para o banco de dados PostgreSQL e outro para a aplicação.

### Deploy no Render

Para o deploy no Render, as variáveis de ambiente devem ser configuradas diretamente no painel do Render. Configure os seguintes valores com base nos dados fornecidos pelo Render (sem incluir informações sensíveis no repositório):

- **DB_URL:** Use a URL interna ou externa conforme orientação do Render.
- **DB_USERNAME, DB_PASSWORD, JWT_SECRET, JWT_EXPIRATION_MS, SERVER_PORT:** Configure conforme seu ambiente de produção.

## Endpoints de API

A aplicação possui endpoints protegidos por autenticação JWT. Para acessar os endpoints, faça o login e utilize o token JWT retornado no header `Authorization` com o prefixo `Bearer`.

### Exemplo de Fluxo de Autenticação

1. **Registro de Usuário:**  
   `POST /auth/register`  
   Corpo da requisição:
   ```json
   {
     "nomeCompleto": "João Silva",
     "email": "joao.silva@example.com",
     "cpf": "12345678901",
     "senha": "sua_senha_segura"
   }
   ```

2. **Login:**  
   `POST /auth/login`  
   Corpo da requisição:
   ```json
   {
     "email": "joao.silva@example.com",
     "senha": "sua_senha_segura"
   }
   ```
   Resposta:
   ```json
   {
     "token": "seu_jwt_token"
   }
   ```

3. **Acesso aos Endpoints Protegidos:**  
   Utilize o token JWT no header da requisição:
   ```
   Authorization: Bearer seu_jwt_token
   ```

4. **Documentação da API:**  
   Acesse a documentação interativa do Swagger em:  
   [https://fiscalsystem-backend.onrender.com/swagger-ui/index.html](https://fiscalsystem-backend.onrender.com/swagger-ui/index.html)

## Estrutura do Projeto

O projeto está organizado em diversas camadas, incluindo:

- **Controller:** Responsável por expor os endpoints da API (ex.: `AuthController`, `CalculoController`, `PdfController`).
- **Service:** Lógica de negócios e operações principais (ex.: `CalculoHonorariosService`, `PdfHonorariosService`, `PdfFepaService`, `PdfPjService`, `PdfRraService`).
- **DTOs:** Objetos para transferência de dados entre a API e os clientes (ex.: `CalculoHonorariosRequest`, `CalculoHonorariosResult`, etc.).
- **Security:** Configurações de autenticação e autorização utilizando JWT.
- **Util:** Classes utilitárias (ex.: `FormatUtils`, `TaxCalculationUtils`).

## Considerações Finais

Este projeto surgiu de uma curiosidade e do desejo de resolver um problema real enfrentado por pessoas próximas a mim. Foi um teste das minhas capacidades de desenvolver uma ferramenta útil, que atenda às necessidades do usuário e resolva problemas concretos. Embora a API possa parecer, em um primeiro momento, uma espécie de "planilha tunada", ela foi projetada como uma estrutura robusta e escalável. Essa estrutura permite a adição de novas funcionalidades, recursos aprimorados e validações mais complexas, superando as limitações de uma simples planilha do Excel.
