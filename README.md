# Authorizer
## _Autorizador de transações_

## Features

- Autorização das transações mediante saldo disponível na balance
- Token de idempotência por meio de filtro que intercepta a request
- Controle de concorrência utilizando cache distribuído do Redis
- Fallback para descontar da balance cash, caso account nao possua saldo disponível na balance
- Cache nas consultas que pesquisam no banco de dados por `merchant name` e `mcc`, dando assim mais performance no processo de autorização, pois são informações que dificilmente vão mudar.


## Requisitos

- Apache Maven 3.8.6
- Java 17
- Docker version 27.3.1, build ce12230
- Docker Compose version v2.14.2
- Git version 2.34.1

## Instalação
- Realizar git clone do repositório

Para execução do projeto é necessário:
- PostgreSQL
- Redis (Cache Distribuído)
- Redis-insight (UI para Redis)

Para subir toda a infra necessária, so executar no diretório `docker`:

```sh
cd docker
docker compose up -d
```

Dados de conexão do database:
```sh
user: miniautorizador
password: miniautorizador
database: miniautorizador
port: 5432
```
Visualização da [user Interface](http://0.0.0.0:5540/) para o redis.

[![N|Solid](https://github.com/juancarllos88/caju-authorizer/blob/main/img/redis-exemplo.png?raw=true)](https://github.com/juancarllos88/caju-authorizer)

## Autorizador
Para execução do autorizador executar:

```sh
mvn spring-boot:run
```

Para execução dos testes unitários:

```sh
mvn clean test
```

Para execução dos testes de integração utilizando:
- PostgreSQL testcontainer
- Redis testcontainer

```sh
mvn clean verify -Dsurefire.skip=true
```

Visualização da [documentação swagger](http://localhost:8080/api/docs/swagger-ui/index.html#)

[![N|Solid](https://github.com/juancarllos88/caju-authorizer/blob/main/img/swagger.png?raw=true)](https://github.com/juancarllos88/caju-authorizer)


## Dados para testes

Inicialmente foi criado uma migration usando a ferramenta de versionamento `flyway`, contendo duas `accounts` e suas respectivas `balances`:
- Account `554e590b-a4bc-4859-b245-cbb4701fdbbd` com 3 balances `FOOD|MEAL|CASH` no valor de R$ 1000,00 cada
- Account `fcd31a0b-3bfa-4996-9f74-74d51e98ed10` com 2 balances `FOOD|MEAL` no valor de R$ 1000,00 cada


Exemplo curl para submeter uma transação para o autorizador:
```sh
curl --location 'http://localhost:8080/transactions/authorization' \
--header 'Content-Type: application/json' \
--data '{
	"id": "687be0b2-8fa1-416d-8c63-565c84a6c502",
    "account": "554e590b-a4bc-4859-b245-cbb4701fdbbd",
	"totalAmount": 1.00,
	"mcc": "5811",
	"merchant": "PADARIA DO ZE               SAO PAULO BR"
}'
```



## Resposta da questão L4

Para cobrir cenário de concorrência implementei duas features:
##### 1. Idempotency Token

[![N|Solid](https://github.com/juancarllos88/caju-authorizer/blob/main/img/idpot.png?raw=true)](https://github.com/juancarllos88/caju-authorizer)

Para deixar o autorizador idempotente, ou seja, o mesmo receber a transação repetidas diversas, mas apenas a primeira vez aplicada é a que realmente altera o resultado, implementei o cenário no qual o adquirente enviaria um token de idempotência no header da requisição, para que do lado do autorizador ele possa fazer o cache da resposta desse pedido. Pois se por algum motivo o adquirente mandar a mesma request utilizando algum política de retentativa, um filtro no autorizador intercepta a requisição e verificar se a transação já foi processada utilizando esse token. Caso a transação já foi processada, o autorizador devolverá o payload de resposta que está armazenado no cash.

[![N|Solid](https://github.com/juancarllos88/caju-authorizer/blob/main/img/l4-1.png?raw=true)](https://github.com/juancarllos88/caju-authorizer)

Exemplo utilizando curl:
```sh
curl --location 'http://localhost:8080/transactions/authorization' \
--header 'idempotency-token: 5d022c17-b6fa-4398-8ee5-a20f5d860e15' \
--header 'Content-Type: application/json' \
--data '{
	"id": "d3ed2893-009c-4626-8c58-e2a64d9fd86a",
    "account": "554e590b-a4bc-4859-b245-cbb4701fdbbd",
	"totalAmount": 1.00,
	"mcc": "5811",
	"merchant": "PADARIA DO ZE               SAO PAULO BR"
}'
```

Pode ser usado o swagger pra testar a feature:

[![N|Solid](https://github.com/juancarllos88/caju-authorizer/blob/main/img/l4-4.png?raw=true)](https://github.com/juancarllos88/caju-authorizer)

##### 2. Transações simultâneas
Para controle de concorrência implementei utilizando a estrutura de cache distribuído do Redis, controlando o saldo da respectiva balance da account no cache, utilizando o seguinte cenário:
- Uma das transações terá prioridade em relação a outra(em milésimo de segundo) na escrita do saldo da balance no cache.
- Enquanto uma está atualizando, a outra aguardará o cache ficar disponível com o novo valor do saldo, para assim efetuar seu processamento.
- A primeira que atualizou o cache segue o fluxo e sensibiliza o saldo no banco de dados.
- Em seguida a outra seguirá o fluxo mantendo a consistência do saldo.

[![N|Solid](https://github.com/juancarllos88/caju-authorizer/blob/main/img/l4-2.png?raw=true)](https://github.com/juancarllos88/caju-authorizer)

[![N|Solid](https://github.com/juancarllos88/caju-authorizer/blob/main/img/l4-3.png?raw=true)](https://github.com/juancarllos88/caju-authorizer)


