# Enterprise RAG Backend

A production-grade, Secure Multi-Tenant Retrieval-Augmented Generation (RAG) Platform built with Java, Spring Boot 3, PostgreSQL (pgvector), and RabbitMQ. 

This architecture moves beyond basic AI wrappers by implementing robust asynchronous document ingestion pipelines, advanced hybrid retrieval algorithms, and enterprise-level observability.

---

## 🏗️ Repository Architecture & Access Rules

This project strictly follows a **Multi-Module Maven** structure to enforce Clean Architecture principles and prevent circular dependencies. To avoid Git merge conflicts, the codebase is partitioned.

    enterprise-rag-backend/
    ├── pom.xml                     (Root POM: Manages dependency versions)
    ├── docker-compose.yml          (Infra orchestration)
    │
    ├── rag-api/                    (MODULE 1: Contracts & Interfaces)
    ├── rag-core/                   (MODULE 2: Domain & Business Logic)
    ├── rag-infrastructure/         (MODULE 3: External Systems)
    └── rag-web/                    (MODULE 4: Presentation & Entry Point)


### 1. `rag-api` (Shared Contracts)
* **What it does:** Contains only Data Transfer Objects (DTOs) and global Interfaces/Exceptions. It has zero business logic.
* **Who accesses it:** 
  * **Ayush (Dev 2):** Primary owner. Creates objects like `ChatRequest`, `ChatResponse`, and `DocumentUploadResponse`.
  * **Suchismita (Dev 1):** Reads from this module to ensure her background workers return the correct status objects.
* **The Rule:** If a payload structure needs to change, both developers must agree, update this module, and merge to `main` first.

### 2. `rag-core` (Domain Logic)
* **What it does:** The brain of the application. Contains business rules, entities, and security logic. It does not know about databases or HTTP.
* **Who accesses it & How:** Shared module, strictly partitioned by package.
  * **Suchismita** works exclusively inside `src/main/java/com/rag/core/ingestion/`. She writes the business logic for how a document should be chunked and parsed.
  * **Ayush** works exclusively inside `src/main/java/com/rag/core/search/` and `com/rag/core/security/`. He writes the business logic for tenant isolation, JWT validation, and search ranking.

### 3. `rag-infrastructure` (External Systems)
* **What it does:** Implements the core logic's interfaces to talk to PostgreSQL, RabbitMQ, Redis, and external LLM APIs (Cohere/OpenAI).
* **Who accesses it & How:** Shared module, strictly partitioned by package.
  * **Suchismita** works inside `com/rag/infra/messaging` (RabbitMQ listeners/DLQ configurations), `com/rag/infra/tika` (PDF parsing implementations), and `com/rag/infra/persistence/ingestion` (JDBC Batch inserts for vectors).
  * **Ayush** works inside `com/rag/infra/cache` (Redis Semantic Cache implementations), `com/rag/infra/llm` (Spring AI ChatClient execution), and `com/rag/infra/persistence/search` (Native SQL CTEs for Hybrid Search).

### 4. `rag-web` (Entry Point)
* **What it does:** The Spring Boot `@SpringBootApplication` runner, REST Controllers, and global HTTP configuration.
* **Who accesses it:** 
  * **Ayush (Dev 2):** Primary owner. Handles all incoming HTTP traffic, routes file uploads to Suchismita's RabbitMQ producers, configures Server-Sent Events (SSE) for streaming chat, and manages OpenTelemetry configurations.

---

## 👥 Developer Roles & Responsibilities

### Developer 1: Ingestion & Data Pipeline Engineer (Suchismita)
**Focus:** Moving unstructured data from the user into the database securely and accurately.
* **Core Tasks:**
  * Configure RabbitMQ Dead Letter Queues (DLQ) for fault-tolerant asynchronous processing.
  * Integrate Apache Tika for structural document parsing.
  * Implement Parent-Child semantic chunking strategies.
  * Execute high-performance JDBC batch inserts into PostgreSQL `pgvector`.

### Developer 2: API, Security & Search Engineer (Ayush)
**Focus:** Protecting the endpoints, managing state, and executing high-accuracy hybrid retrieval.
* **Core Tasks:**
  * Implement stateless JWT authentication with a Redis-backed token revocation blacklist.
  * Engineer a Token-Bucket API rate limiter.
  * Write complex native SQL queries for Hybrid Search (BM25 + Vector similarity) using Reciprocal Rank Fusion (RRF).
  * Build the Redis Semantic Cache and stream LLM responses via Server-Sent Events (SSE).

---

## 🔄 Git Workflow & Collaboration Rules

To guarantee smooth execution and zero blocking merge conflicts, the following rules apply:

1. **Strict Feature Branching:** Never commit directly to `main`. Create isolated branches for your tasks:
   * `git checkout -b feature/suchismita-rabbitmq-ingestion`
   * `git checkout -b feature/ayush-jwt-security`
2. **Package Isolation:** You must only edit files within your assigned domain/infrastructure sub-packages.
3. **Isolated Configurations:** Avoid modifying the global `application.yml` simultaneously. Use Java-based `@Configuration` classes in your specific modules (e.g., `RabbitMQConfig.java` for Dev 1, `SecurityConfig.java` for Dev 2).

---

## 🚀 Local Development Setup

1. **Start the Infrastructure:**
   Ensure Docker Desktop is running, then spin up the backing services:
   ```bash
   docker-compose up -d
   ```
2. **Install Dependencies:**
   Compile the multi-module project from the root directory:
   ```bash
   mvn clean install
   ```
3. **Run the Application:**
   Navigate to the web module and start the Spring Boot server:
   ```bash
   cd rag-web
   mvn spring-boot:run
   ```
