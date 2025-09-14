# Mini Autorizador

## Visão Geral

O Mini Autorizador é uma aplicação Spring Boot que simula um sistema simplificado de autorização de transações para cartões pré-pagos. O sistema permite a criação de cartões, consulta de saldo e processamento de transações de débito, aplicando regras de autorização para validar as transações.

## Arquitetura Técnica

O projeto foi desenvolvido utilizando as seguintes tecnologias e padrões:

### Tecnologias

- **Java 8+**
- **Spring Boot**: Framework para criação de aplicações Java
- **Spring MVC**: Para desenvolvimento da API REST
- **Spring Data JPA**: Para persistência de dados
- **Hibernate**: Como implementação JPA
- **MySQL/MongoDB**: Bancos de dados suportados
- **Docker**: Para containerização da aplicação e dependências
- **JUnit 5**: Para testes automatizados
- **Maven**: Para gerenciamento de dependências e build

### Padrões de Projeto e Arquitetura

- **Domain-Driven Design (DDD)**: Utilização de Value Objects para encapsular regras de domínio
- **REST API**: Interface de comunicação baseada em HTTP
- **Chain of Responsibility**: Para validação de regras de autorização de transações
- **Single Responsibility Principle (SRP)**: Cada classe tem uma única responsabilidade
- **Optimistic Locking**: Para controle de concorrência nas transações

## Principais Funcionalidades

### 1. Gerenciamento de Cartões

- **Criação de Cartões**: Permite criar novos cartões com saldo inicial de R$ 500,00
- **Consulta de Saldo**: Permite consultar o saldo disponível em um cartão

### 2. Processamento de Transações

- **Autorização de Transações**: Processa transações de débito aplicando regras de autorização
- **Regras de Autorização**:
  - Verificação da existência do cartão
  - Validação da senha do cartão
  - Verificação de saldo suficiente

## Como Executar a Aplicação

### Pré-requisitos

- Java 8 ou superior
- Docker e Docker Compose
- Maven (opcional, caso queira compilar o projeto)

### Passos para Execução

1. **Clone o repositório**:
   ```
   git clone [URL_DO_REPOSITORIO]
   cd mini-autorizador
   ```

2. **Inicie os containers Docker**:
   ```
   run_docker.bat
   ```
   Isso iniciará os containers do MySQL e MongoDB configurados para a aplicação.

3. **Compile e execute a aplicação** (caso não esteja usando a versão já compilada):
   ```
   mvn clean package
   java -jar target/mini-autorizador-0.0.1-SNAPSHOT.jar
   ```

4. **Acesse a aplicação**:
   A API estará disponível em `http://localhost:8080`

## Documentação da API

### Endpoints de Cartões

#### Criar Cartão
- **URL**: `/cartoes`
- **Método**: `POST`
- **Autenticação**: Basic Auth (user/password)
- **Corpo da Requisição**:
  ```json
  {
    "numeroCartao": "1234567890123456",
    "senha": "1234"
  }
  ```
- **Respostas**:
  - `201 Created`: Cartão criado com sucesso
  - `422 Unprocessable Entity`: Cartão já existente

#### Consultar Saldo
- **URL**: `/cartoes/{numeroCartao}`
- **Método**: `GET`
- **Autenticação**: Basic Auth (user/password)
- **Respostas**:
  - `200 OK`: Retorna o saldo do cartão
  - `404 Not Found`: Cartão não encontrado

### Endpoints de Transações

#### Realizar Transação
- **URL**: `/transacoes`
- **Método**: `POST`
- **Autenticação**: Basic Auth (user/password)
- **Corpo da Requisição**:
  ```json
  {
    "numeroCartao": "1234567890123456",
    "senhaCartao": "1234",
    "valor": 10.00
  }
  ```
- **Respostas**:
  - `201 Created`: Transação realizada com sucesso (retorna "OK")
  - `422 Unprocessable Entity`: Transação não autorizada (retorna o motivo)

## Testes

O projeto inclui vários tipos de testes:

### Testes Unitários
Testam componentes individuais isoladamente.

### Testes de Integração
Testam a integração entre componentes, como serviços e repositórios.

### Testes End-to-End (E2E)
Testam o fluxo completo da aplicação, simulando requisições HTTP reais.

### Testes de Concorrência
Testam o comportamento da aplicação em cenários de acesso concorrente.

Para executar os testes:
```
mvn test
```

## Estrutura do Projeto

```
mini-autorizador/
├── docker/                     # Configurações Docker
│   ├── docker-compose.yml      # Definição dos serviços
│   └── scripts/                # Scripts de inicialização
├── src/
│   ├── main/
│   │   ├── java/com/example/miniautorizador/
│   │   │   ├── config/         # Configurações da aplicação
│   │   │   ├── controller/     # Controladores REST
│   │   │   ├── domain/         # Value Objects
│   │   │   ├── dto/            # Objetos de Transferência de Dados
│   │   │   ├── enums/          # Enumerações
│   │   │   ├── exception/      # Exceções personalizadas
│   │   │   ├── model/          # Entidades JPA
│   │   │   ├── repository/     # Repositórios de dados
│   │   │   ├── service/        # Serviços de negócio
│   │   │   │   └── authorization/ # Regras de autorização
│   │   │   └── MiniAutorizadorApplication.java # Classe principal
│   │   └── resources/
│   │       └── application.properties # Configurações da aplicação
│   └── test/                   # Testes automatizados
├── pom.xml                     # Configuração Maven
└── run_docker.bat              # Script para iniciar containers
```

## Considerações de Segurança

- A aplicação utiliza autenticação básica (Basic Auth) para proteger os endpoints
- As senhas dos cartões são armazenadas de forma segura (hash)
- Implementação de controle de concorrência para evitar condições de corrida em transações simultâneas

## Melhorias Futuras

- Implementação de documentação interativa com Swagger/OpenAPI
- Adição de mais regras de autorização
- Implementação de monitoramento e métricas
- Melhoria na segurança com JWT ou OAuth2
- Implementação de cache para melhorar a performance