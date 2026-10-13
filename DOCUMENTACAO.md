# Documentação da Solução — Comunicação por Eventos

## 1. Visão geral

A plataforma usa comunicação **assíncrona** via **Apache Kafka**. O `contrato-service` publica eventos do ciclo de vida dos contratos; os serviços de notificação, reputação e auditoria reagem consumindo esses eventos.

```text
                                     +-------------------+
                                     |      Cliente      |
                                     +---------+---------+
                                               |
                                               | HTTP
                                               v
                                     +-------------------+
                                     |    API Gateway    |
                                     |       :8080       |
                                     +---------+---------+
                                               |
                                               | HTTP
                                               v
                                     +-------------------+
                                     |  contrato-service |
                                     |       :8081       |
                                     +---------+---------+
                                               |
                                               | publica eventos
                                               v
                                     +-------------------+
                                     |       Kafka       |
                                     |  contrato.events  |
                                     +---------+---------+
                                +------------+------------+
                                |            |            |
                                v            v            v
                      +----------------+ +----------------+ +----------------+
                      |  notificacao   | |  reputacao     | |  auditoria     |
                      |  service:8082  | |  service:8083  | |  service:8084  |
                      +----------------+ +----------------+ +----------------+
```

## 2. Serviços participantes

| Serviço             | Papel                                  | Porta | Banco          |
| ------------------- | -------------------------------------- | ----- | -------------- |
| api-gateway         | ponto de entrada HTTP                  | 8080  | —              |
| contrato-service    | produtor de eventos                    | 8081  | contrato_db    |
| notificacao-service | consumidor (notificações)              | 8082  | notificacao_db |
| reputacao-service   | consumidor (reputação)                 | 8083  | reputacao_db   |
| auditoria-service   | consumidor (registra todos os eventos) | 8084  | auditoria_db   |

## 3. Tópicos Kafka

| Tópico                | Uso                                  |
| --------------------- | ------------------------------------ |
| `contrato.events`     | eventos do ciclo de vida do contrato |
| `contrato.events.dlt` | mensagens com erro (dead letter)     |

## 4. Eventos

| Evento              | Produtor         | Consumidores                      |
| ------------------- | ---------------- | --------------------------------- |
| `ContratoCriado`    | contrato-service | notificacao, auditoria            |
| `EntregaRegistrada` | contrato-service | notificacao, auditoria            |
| `ContratoConcluido` | contrato-service | notificacao, reputacao, auditoria |
| `ContratoCancelado` | contrato-service | notificacao, auditoria            |

## 5. Formato das mensagens

Todas as mensagens são **JSON**. Campos comuns a todos os eventos:

| Campo           | Tipo               | Descrição                     |
| --------------- | ------------------ | ----------------------------- |
| `eventoId`      | UUID               | identificador único do evento |
| `tipoEvento`    | String             | tipo do evento                |
| `ocorridoEm`    | Instant (ISO-8601) | data/hora do evento           |
| `contratoId`    | UUID               | contrato relacionado          |
| `correlationId` | String             | correlação da operação        |

Campos específicos: `clienteId`, `freelancerId`, `titulo`, `valor` (conforme o evento).

Exemplo (`ContratoCriado`):

```json
{
  "eventoId": "5013d3aa-109d-4ed7-a461-4b1e906c4436",
  "tipoEvento": "ContratoCriado",
  "ocorridoEm": "2026-10-13T22:35:21.285Z",
  "contratoId": "8e6cd7a7-b3d4-4dcf-a047-522a328b98d0",
  "clienteId": "11111111-1111-1111-1111-111111111111",
  "freelancerId": "22222222-2222-2222-2222-222222222222",
  "titulo": "Contrato",
  "valor": 3500.0,
  "correlationId": "teste-contrato-007"
}
```

## 6. Estratégia de particionamento

- **Chave de partição**: `contratoId`.
- Eventos do **mesmo contrato** caem na **mesma partição** → ordem preservada.
- Contratos **distintos** processados em **paralelo** (`concurrency = 3`)

## 7. Estratégia de idempotência

- Cada evento tem `eventoId` único.
- Consumidores registram os IDs processados na tabela `eventos_processados` (notificacao e reputacao).
- Auditoria usa restrição única em `eventId`.
- Mensagem reprocessada é ignorada sem duplicar registros.

## 8. Publicação transacional

Padrão **Transactional Outbox**:

1. O evento é gravado na tabela `outbox_eventos` **na mesma transação** do banco.
2. O `OutboxRelay` (agendado) lê os eventos pendentes e publica no Kafka.

## 9. Centralização de logs

- **Loki + Grafana** (Docker).

## 10. Tracing

- **Brave + Zipkin**

## 11. Inicialização da infraestrutura

```bash
cd infra
docker compose up -d
```

Subir os serviços (um terminal para cada):

```bash
mvn -pl eureka-server spring-boot:run
mvn -pl api-gateway spring-boot:run
mvn -pl contrato-service spring-boot:run
mvn -pl notificacao-service spring-boot:run
mvn -pl reputacao-service spring-boot:run
mvn -pl auditoria-service spring-boot:run
```

Ferramentas:

- Kafka UI: http://localhost:8090
- Zipkin: http://localhost:9411
- Grafana: http://localhost:3000

## 12. Exemplos de chamadas

```bash
# criar contrato
curl -X POST http://localhost:8080/contratos \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: teste-001' \
  -d '{"clienteId":"11111111-1111-1111-1111-111111111111","freelancerId":"22222222-2222-2222-2222-222222222222","titulo":"API de pagamentos","valor":3500.00}'

# registrar entrega
curl -X POST http://localhost:8080/contratos/{id}/entrega -H 'X-Correlation-Id: teste-001'

# concluir
curl -X POST http://localhost:8080/contratos/{id}/concluir -H 'X-Correlation-Id: teste-001'

# cancelar
curl -X POST http://localhost:8080/contratos/{id}/cancelar -H 'X-Correlation-Id: teste-001'
```

Consultas:

```bash
curl http://localhost:8080/contratos
curl http://localhost:8080/api/notificacoes
curl http://localhost:8080/api/reputacoes
curl http://localhost:8080/api/auditoria
```
