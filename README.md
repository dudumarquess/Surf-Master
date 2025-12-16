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
