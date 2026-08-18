<div align="center">

# UITax — Unemployment Insurance Tax Microservices Platform

**A 5-service, event-driven Java microservices system** demonstrating service discovery, API gateway routing, the transactional outbox pattern, Kafka-based messaging, idempotent processing, circuit breakers, and dead-letter handling.

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](.)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5%20%2F%204.1-6DB33F?logo=springboot)](.)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Eureka%20%7C%20Gateway-6DB33F)](.)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-231F20?logo=apachekafka)](.)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql)](.)
[![Resilience4j](https://img.shields.io/badge/Resilience4j-Circuit%20Breaker-FF6B6B)](.)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](.)

📄 [**Full technical documentation (PDF)**](./docs/UITax-Project-Documentation.pdf) — architecture, database schema, every configuration file, and a complete log of real issues encountered and resolved during development.

</div>

---

## Overview

UITax simulates a simplified state unemployment insurance tax filing and payment workflow — an employer submits a filing, the system reliably records it, and independent downstream services react to that event to process payment and send notifications. It was built as a hands-on project to gain **production-grade experience with distributed systems patterns** that are difficult to learn from documentation alone: reliable event delivery, service-to-service resilience, and horizontal decoupling via message-driven architecture.

Every pattern below was not just implemented, but **deliberately broken and re-verified** — including a real Spring AOP self-invocation bug that silently disabled a circuit breaker, diagnosed and fixed through direct observation of Actuator metrics and event logs.

## Architecture

```mermaid
flowchart TD
    Client([Postman / Client]) -->|HTTP POST| Gateway[API Gateway<br/>:8080]

    Gateway --> TaxFiling[tax-filing-service<br/>:8081]
    Gateway --> Payment[payment-service<br/>:8082]
    Notification[notification-service<br/>:8083]

    Eureka[(Eureka Server<br/>:8761)] -. register/discover .-> Gateway
    Eureka -.-> TaxFiling
    Eureka -.-> Payment
    Eureka -.-> Notification

    TaxFiling -->|JPA save filing + outbox row| FilingDB[(filingdb<br/>Postgres :5433)]
    TaxFiling -->|poller publishes every 5s| Kafka{{Kafka topic<br/>tax-filing-events}}

    Kafka -->|consumer group:<br/>payment-service-group| Payment
    Kafka -->|consumer group:<br/>notification-service-group| Notification

    Payment -->|circuit breaker| Gateway2[Simulated External<br/>Payment Gateway]
    Payment --> PaymentDB[(paymentdb<br/>Postgres :5434)]
    Payment -. on repeated failure .-> DLT{{Dead Letter Topic<br/>tax-filing-events.DLT}}

    style Client fill:#eee,stroke:#555
    style Gateway fill:#fbe9da,stroke:#c1650a
    style TaxFiling fill:#dce9f7,stroke:#1f3a5f
    style Payment fill:#dce9f7,stroke:#1f3a5f
    style Notification fill:#dce9f7,stroke:#1f3a5f
    style Eureka fill:#ede7f6,stroke:#5b3a8c
    style Kafka fill:#fff3cd,stroke:#8a6d00
    style DLT fill:#f8e3e1,stroke:#b03a2e
    style FilingDB fill:#e3f2e9,stroke:#2e8b57
    style PaymentDB fill:#e3f2e9,stroke:#2e8b57
    style Gateway2 fill:#f8e3e1,stroke:#b03a2e
```

## Engineering Highlights

- **Solved the dual-write problem** using the transactional outbox pattern — filing data and its corresponding event are written atomically in a single database transaction, with a scheduled poller independently guaranteeing at-least-once delivery to Kafka.
- **Implemented idempotent event consumption**, closing a real race-condition gap in a naive "check-then-insert" approach by relying on a database uniqueness constraint as the atomic safety net rather than application-level logic.
- **Diagnosed and fixed a Spring AOP self-invocation bug** that silently disabled a `@CircuitBreaker` annotation — refactored the affected call into a dedicated Spring bean so it correctly passes through the framework's proxy, then verified the full CLOSED → OPEN → HALF_OPEN state cycle via Actuator metrics.
- **Built a Dead Letter Topic recovery path** so that messages exhausting all retries are preserved for investigation instead of being silently discarded — verified with a forced-failure test.
- **Resolved a real Spring Cloud / Spring Boot version incompatibility** across services running different framework versions, correctly pairing each service's release train.
- **Implemented service discovery and gateway-based routing** end-to-end with Netflix Eureka and Spring Cloud Gateway, including diagnosing and fixing an environment-specific hostname resolution bug on Windows.

*(Full write-up of these and 15 total real issues, with root cause and fix for each, is in the [project documentation](./docs/UITax-Project-Documentation.pdf).)*

## Services

| Service | Port | Responsibility |
|---|---|---|
| `eureka-server` | 8761 | Service registry and discovery |
| `api-gateway` | 8080 | Single entry point; routes requests to services by name via Eureka |
| `tax-filing-service` | 8081 | Accepts filings; persists via the transactional outbox pattern; publishes domain events to Kafka |
| `payment-service` | 8082 | Consumes filing events; processes payment idempotently; circuit-breaker-protected; backed by a Dead Letter Topic |
| `notification-service` | 8083 | Independently consumes the same events under a separate consumer group and logs them |

## Key Patterns Demonstrated

| Pattern | Purpose |
|---|---|
| Database-per-service | Each service owns its own PostgreSQL instance; no cross-service queries or shared schemas |
| Transactional Outbox | Guarantees reliable event publication without a distributed transaction |
| Kafka pub/sub, multiple consumer groups | Independent, decoupled reactions to the same domain event |
| Idempotent processing | Safe against Kafka's at-least-once delivery semantics |
| Circuit Breaker (Resilience4j) | Fails fast against a degraded dependency instead of cascading failure |
| Dead Letter Topic | No message is ever silently lost after exhausting retries |
| Service Discovery + API Gateway | No hardcoded service addresses; single client-facing entry point |

## Tech Stack

**Language & Framework:** Java 25 · Spring Boot 3.5 / 4.1 · Spring Cloud (Netflix Eureka, Gateway)
**Messaging & Data:** Apache Kafka · Zookeeper · PostgreSQL 16 · Spring Data JPA / Hibernate
**Resilience:** Resilience4j · Spring Kafka `DefaultErrorHandler` + `DeadLetterPublishingRecoverer`
**Infrastructure:** Docker Compose · Kafka UI
**Tooling:** Maven · Postman · DBeaver

## Quick Start

```bash
# 1. Start shared infrastructure — Postgres x2, Kafka, Zookeeper, Kafka UI
cd infra
docker compose up -d

# 2. Start each service, in order (from each service's own directory)
mvn spring-boot:run
```

**Start order matters:** `eureka-server` → `tax-filing-service` → `payment-service` → `notification-service` → `api-gateway`.

**Verify:**
- Eureka dashboard: `http://localhost:8761` — all 5 services should show `UP`
- Kafka UI: `http://localhost:8090`

**Submit a test filing through the gateway:**
```bash
curl -X POST http://localhost:8080/tax-filing-service/api/filings \
  -H "Content-Type: application/json" \
  -d '{
        "employerId": "3f7b1a2e-4c5d-4e6f-8a9b-1c2d3e4f5a6b",
        "filingPeriod": "2026-Q3",
        "wagesReported": 60000.00,
        "taxDue": 5200.00
      }'
```

Watch the `payment-service` and `notification-service` logs — both independently process the same event within a few seconds.

## Roadmap

- [ ] Automated test suite (JUnit, Mockito, Testcontainers)
- [ ] Global exception handling (`@RestControllerAdvice`)
- [ ] Containerize the application services (Dockerfile per service)
- [ ] CI/CD pipeline (Jenkins) — build, test, image publish
- [ ] Deploy to Kubernetes
- [ ] Observability stack (Prometheus + Grafana) — outbox backlog, circuit breaker state, consumer lag
- [ ] Authentication / authorization
- [ ] OpenAPI / Swagger documentation

## Documentation

The [full project documentation](./docs/UITax-Project-Documentation.pdf) includes the complete high-level and low-level design, database schema, every `application.yml`, all key code with explanations, a detailed log of 15 real issues encountered during development with root cause and resolution for each, and the manual testing strategy used to verify every feature end-to-end.

---

<div align="center">

Built by **Vinay Manavarthi** — Java Full Stack Developer

</div>
