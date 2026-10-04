<div align="center">

# ⚡ FinFlow Core

**Production-Grade Banking & Financial Aggregation REST Platform (Spring Boot 3.3, Java 17, Spring Security 6 JWT, PostgreSQL)**

[![Java](https://img.shields.io/badge/Java-17-0891b2?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-0891b2?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-0891b2?style=flat-square&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-0891b2?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--Stage-0891b2?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![CI](https://img.shields.io/badge/CI-GitHub_Actions-0891b2?style=flat-square&logo=githubactions&logoColor=white)](https://github.com/ronitgupta138/finflow-core/actions)

</div>

---

## 📌 Architectural Highlights

* **Modern Security Architecture:** Stateless authentication via **Spring Security 6 (Lambda DSL)** and modern JJWT (`0.12.6`) signed tokens with secure claim extraction.
* **Granular User Isolation:** Complete multi-tenant privacy where transactions, categories, and financial analytics are strictly isolated per authenticated user ID.
* **Scheduled Analytics Engine:** Automated background rate polling and market benchmarks with `@Scheduled` task execution.
* **Real-time Monthly Aggregations:** Custom optimized JPQL aggregation queries delivering monthly income, expense totals, net savings, and category distribution percentages.
* **Standardized Error Handling:** Global `@RestControllerAdvice` implementing RFC 7807 problem details with strict validation error mapping.
* **Cloud-Native Deployment:** Multi-stage `Dockerfile` with dynamic port binding (`${PORT:-8080}`) and zero-dependency `docker-compose` orchestration.

---

## 🏛️ System Architecture

```
[ Client / Web / Mobile ]
           │
           │ (HTTPS / Bearer JWT)
           ▼
┌───────────────────────────────────────────────────────────┐
│                   FinanceFlow Gateway                     │
│  - JwtAuthenticationFilter  - SecurityFilterChain (Stateless) │
└────────────────────────────┬──────────────────────────────┘
                             │
       ┌─────────────────────┼─────────────────────┐
       ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ Auth Service │      │  Tx Service  │      │ Analytics    │
│ (BCrypt/JWT) │      │  (CRUD/JPA)  │      │ (Aggregates) │
└──────┬───────┘      └──────┬───────┘      └──────┬───────┘
       │                     │                     │
       └─────────────────────┼─────────────────────┘
                             │
                             ▼
              ┌─────────────────────────────┐
              │    PostgreSQL 16 Engine     │
              │  (HikariCP Connection Pool) │
              └─────────────────────────────┘
```

---

## 🚀 Quick Start (Docker Compose)

Clone the repository and spin up the complete API stack and PostgreSQL database in seconds:

```bash
git clone https://github.com/ronitgupta138/finance-flow-api.git
cd finance-flow-api

# Start services
docker compose up --build -d

# Inspect health check
curl http://localhost:8080/api/health
```

---

## 📡 REST API Reference

### 🔐 1. Authentication
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new user account | ❌ No |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT Bearer token | ❌ No |

### 🏷️ 2. Categories
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/categories` | List user's expense categories | 🔒 Bearer |
| `POST` | `/api/categories` | Create new category with budget | 🔒 Bearer |
| `DELETE` | `/api/categories/{id}` | Remove custom category | 🔒 Bearer |

### 💳 3. Transactions
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/transactions` | List all transactions (supports `startDate` & `endDate`) | 🔒 Bearer |
| `POST` | `/api/transactions` | Log an income or expense transaction | 🔒 Bearer |
| `DELETE` | `/api/transactions/{id}` | Delete transaction record | 🔒 Bearer |

### 📊 4. Analytics & Summaries
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/analytics/monthly-summary?year=2026&month=10` | Monthly total, net savings & category breakdown % | 🔒 Bearer |

---

## 🧪 Running Unit & Integration Tests

```bash
# Run test suite
mvn clean test
```

---

## 📜 License
This project is open-source under the [MIT License](LICENSE).
