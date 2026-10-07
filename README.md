<div align="center">

```text
███████╗██╗███╗   ██╗███████╗██╗      ██████╗ ██╗    ██╗  ██████╗ ██████╗ ██████╗ ███████╗
██╔════╝██║████╗  ██║██╔════╝██║     ██╔═══██╗██║    ██║  ██╔════╝██╔═══██╗██╔══██╗██╔════╝
█████╗  ██║██╔██╗ ██║█████╗  ██║     ██║   ██║██║ █╗ ██║  ██║     ██║   ██║██████╔╝█████╗  
██╔══╝  ██║██║╚██╗██║██╔══╝  ██║     ██║   ██║██║███╗██║  ██║     ██║   ██║██╔══██╗██╔══╝  
██║     ██║██║ ╚████║██║     ███████╗╚██████╔╝╚███╔███╔╝  ╚██████╗╚██████╔╝██║  ██║███████╗
╚═╝     ╚═╝╚═╝  ╚═══╝╚═╝     ╚══════╝ ╚═════╝  ╚══╝╚══╝   ╚═════╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝
```

### **FinFlow Core — Production-Grade Banking & Financial Aggregation REST Platform**
#### *With Offline UPI Mesh Payment Settlement Engine*

[![Java](https://img.shields.io/badge/Java-17-0891b2?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-0891b2?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-0891b2?style=flat-square&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-0891b2?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--Stage-0891b2?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![CI](https://img.shields.io/badge/CI-GitHub_Actions-0891b2?style=flat-square&logo=githubactions&logoColor=white)](https://github.com/ronitgupta138/finflow-core/actions)

</div>

---

## 📌 Architectural Highlights

* **Offline UPI Mesh Settlement Engine:** Full store-and-forward peer-to-peer payment settlement over Bluetooth Low Energy (BLE) gossip relays for zero-connectivity environments (basements, remote flights, disaster zones).
* **Zero-Trust Hybrid Cryptography:** RSA-OAEP + AES-256-GCM authenticated payload encryption. Intermediate relay devices cannot read or modify amounts; any byte tampering breaks the AEAD authentication tag and digital signature.
* **"Thundering Herd" Concurrency Defense:** Atomic Compare-and-Swap (CAS) deduplication on the ciphertext SHA-256 digest (`ConcurrentHashMap.putIfAbsent`), proven under 30-thread simultaneous race tests to guarantee exactly-once debit.
* **Modern Security Architecture:** Stateless authentication via **Spring Security 6 (Lambda DSL)** and modern JJWT (`0.12.6`) signed tokens with secure claim extraction.
* **Granular User Isolation:** Complete multi-tenant privacy where transactions, categories, and financial analytics are strictly isolated per authenticated user ID.
* **Scheduled Analytics Engine:** Automated background rate polling and market benchmarks with `@Scheduled` task execution.
* **Real-time Monthly Aggregations:** Custom optimized JPQL aggregation queries delivering monthly income, expense totals, net savings, and category distribution percentages.

---

## 🏛️ System Architecture

```
[ Client / Web / Mobile ]                  [ Offline Phone (No Internet) ]
           │                                              │
           │ (HTTPS / Bearer JWT)                         │ (Signed Hybrid Encrypted Packet)
           ▼                                              ▼
┌─────────────────────────────────────────┐  [ Relay Peer 1 (BLE Mesh Hop 1) ]
│           FinanceFlow Gateway           │               │
│ - JwtAuthFilter   - SecurityFilterChain │               ▼
└────────────────────┬────────────────────┘  [ Relay Peer 2 (BLE Mesh Hop 2) ]
                     │                                    │
    ┌────────────────┼────────────────┐                   ▼
    ▼                ▼                ▼      [ Bridge Phone (Gets 4G/Wi-Fi) ]
┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │
│ Auth Service │ │  Tx Service  │ │ Mesh Engine  │◄───────┘ (POST /api/v1/mesh/ingest)
│ (BCrypt/JWT) │ │  (CRUD/JPA)  │ │ (RSA/AES-GCM)│
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       └────────────────┼────────────────┘
                        │
                        ▼
         ┌─────────────────────────────┐
         │    PostgreSQL 16 Engine     │
         │  (HikariCP Connection Pool) │
         └─────────────────────────────┘
```

---

## 🌐 The Offline UPI Mesh Subsystem

### The 3 Hard Distributed Systems Problems Solved

1. **Zero-Trust Relays:** Sender creates an ephemeral AES-256 key, encrypts the payload with AES-GCM (including 128-bit authentication tag), encrypts the AES key with the server's RSA-OAEP public key, and signs the canonical payload with their private key. Intermediate phones pass the packet blindly without reading or tampering.
2. **Thundering Herd Multi-Bridge Race:** When 50 bridge phones upload the same packet at the exact same millisecond, an atomic CAS gate on `sha256(ciphertext)` ensures exactly 1 thread settles into the database; 49 threads short-circuit as `DUPLICATE_DROPPED`.
3. **Replay & Expiration Defense:** Maximum 24-hour packet age guard, nonces, and sender digital signature verification.

---

## 📡 REST API Reference

### 🔐 1. Authentication
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new user account | ❌ No |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT Bearer token | ❌ No |

### 🌐 2. Offline UPI Mesh Subsystem
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/mesh/public-key` | Fetch server RSA-OAEP public key for offline caching | ❌ No |
| `POST` | `/api/v1/mesh/ingest` | Bridge upload endpoint for encrypted mesh packets | ❌ No |
| `POST` | `/api/v1/mesh/simulate` | Interactive multi-hop BLE gossip payment simulation | ❌ No |
| `GET` | `/api/v1/mesh/logs` | View recent settlement audit logs & deduplication events | ❌ No |

### 🏷️ 3. Categories
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/categories` | List user's expense categories | 🔒 Bearer |
| `POST` | `/api/categories` | Create new category with budget | 🔒 Bearer |
| `DELETE` | `/api/categories/{id}` | Remove custom category | 🔒 Bearer |

### 💳 4. Transactions
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/transactions` | List all transactions (supports `startDate` & `endDate`) | 🔒 Bearer |
| `POST` | `/api/transactions` | Log an income or expense transaction | 🔒 Bearer |
| `DELETE` | `/api/transactions/{id}` | Delete transaction record | 🔒 Bearer |

### 📊 5. Analytics & Summaries
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/analytics/monthly-summary?year=2026&month=10` | Monthly total, net savings & category breakdown % | 🔒 Bearer |

---

## 🧪 Running Unit & Integration Tests

```bash
# Run complete test suite (includes 30-thread Thundering Herd concurrency & crypto tests)
mvn clean test
```

---

## 🚀 Quick Start (Docker Compose)

```bash
# Start complete service stack
docker compose up --build -d

# Check health
curl http://localhost:8080/api/health

# Run an offline mesh simulation
curl -X POST http://localhost:8080/api/v1/mesh/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "senderEmail": "alice@mesh.internal",
    "recipientEmail": "bob@mesh.internal",
    "amount": 150.00,
    "relayCount": 3,
    "note": "Basement Coffee Offline UPI"
  }'
```

---

## 📜 License
This project is open-source under the [MIT License](LICENSE).
