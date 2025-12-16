# 🌊 SurfMaster

**SurfMaster** is a full-stack web platform designed to help surfers decide **when** and **where** to surf by combining surf forecasts, spot metadata, and an LLM-powered conversational assistant.

The project focuses on building a realistic **decision-support system**, integrating backend engineering best practices with modern AI capabilities.

---

## 🚀 Core Features

- 🌍 **Surf Spot Management**
  - CRUD operations for surf spots with geographic information
  - Metadata such as difficulty level, swell direction, and wind preferences

- 🌊 **Forecast & Surf Summary**
  - Aggregation and processing of surf forecast data
  - Computation of surf quality summaries per spot and time window

- 🤖 **LLM-Powered Surf Assistant**
  - Conversational interface for surf-related questions
  - Provider-agnostic LLM integration via environment variables

- 🧩 **Clean Backend Architecture**
  - Separation of concerns across controllers, services, repositories, DTOs, and entities
  - Designed for extensibility, maintainability, and testability

---

## 🛠️ Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Data JPA / Hibernate
- RESTful API
- Maven

### Frontend *(planned / in progress)*
- Angular
- TypeScript

### Infrastructure
- Docker & Docker Compose
- Environment-based configuration
- Relational database (e.g. PostgreSQL)

### AI / LLM
- API-based LLM access
- Secure configuration via environment variables
- No vendor lock-in

---

## 🔐 Configuration & Secrets

All sensitive configuration is handled through **environment variables**.

Example:
```bash
SURF_LLM_API_KEY=your_api_key_here
```

### Stormglass Forecasts (manual sync)

- Configure provider and secrets via environment variables:
  ```bash
  export SURF_FORECAST_PROVIDER=stormglass
  export SURF_FORECAST_STORMGLASS_API_KEY=your_stormglass_key
  export SURF_FORECAST_STORMGLASS_SOURCE=noaa   # opcional
  export SURF_FORECAST_STORMGLASS_HOURS=72      # opcional
  ```
- Call `POST /api/forecasts/sync?spotId=...&from=...&to=...` para baixar somente a janela desejada e persistir os dados. Os endpoints de consulta e recomendação usam o que estiver no banco, evitando ultrapassar o limite diário da API.

### Groq LLM + RAG

- Antes de iniciar o backend, exporte:
  ```bash
  export SURF_LLM_PROVIDER=groq
  export SURF_LLM_MODEL=llama-3.1-8b-instant
  export SURF_LLM_BASE_URL=https://api.groq.com/openai/v1
  export SURF_LLM_EMBEDDING_MODEL=BAAI/bge-base-en-v1.5
  export SURF_LLM_EMBEDDING_ENDPOINT=https://router.huggingface.co/hf-inference/models/{model}/pipeline/feature-extraction
  export SURF_LLM_API_KEY=your_groq_key
  export SURF_HF_API_KEY=your_huggingface_token  # usado para embeddings BGE
  ```
- O chat agora funciona com Retrieval-Augmented Generation: cada pergunta gera embeddings BGE (HuggingFace) para a pergunta e para os spots cadastrados, seleciona os mais relevantes e envia o contexto ao LLM. A resposta sempre referencia apenas o que veio desse contexto, avisando o usuário caso os embeddings não estejam disponíveis.
- A mesma infraestrutura servirá para gerar resumos via LLM (em breve).

## ▶️ Running the Project

### Backend (local)
```bash
cd backend
mvn spring-boot:run
```

### Backend (Docker)
```bash
cd backend
docker-compose up --build
```

The API will be available at:
```bash
http://localhost:8080
```

---

## 🎯 Project Goals
- Apply production-grade backend architecture

- Integrate LLMs responsibly (secure secrets, clean abstractions)

- Build a non-trivial, real-world decision-support application

- Serve as a portfolio project for backend and AI-oriented roles

## 🧠 Project Status
- Backend core: ✅ implemented

- LLM integration:  🚧 in progress

Forecast logic: 🚧 in progress

Frontend: 🚧 in progress

Tests: 🚧 in progress
