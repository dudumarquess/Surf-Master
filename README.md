🌊 SurfMaster

SurfMaster is a full-stack web platform designed to help surfers decide when and where to surf by combining surf forecasts, spot metadata, and an LLM-powered conversational assistant.

The project explores the intersection of backend engineering, data modeling, and LLM integration in a real-world decision-support application.

🚀 Core Features

🌍 Surf Spot Management

CRUD operations for surf spots with geographic data

Spot metadata such as difficulty level, swell and wind preferences

🌊 Forecast & Surf Summary

Aggregation of forecast data from external sources

Computation of surf quality summaries per spot and time window

🤖 LLM-Powered Surf Assistant

Conversational interface to ask questions like:

“Is tomorrow good for beginners?”

“Which spot works best with offshore wind?”

LLM integration abstracted via environment variables (provider-agnostic)

🧩 Clean Architecture

Clear separation between controllers, services, repositories, DTOs, and entities

Designed for extensibility and testability

🛠️ Tech Stack
Backend

Java 17

Spring Boot

Spring Data JPA / Hibernate

RESTful API

Maven

Frontend (planned / in progress)

Angular

TypeScript

REST API consumption

Infrastructure

Docker & Docker Compose

Environment-based configuration

PostgreSQL (or compatible relational DB)

AI / LLM

LLM access via API key (provider configurable)

Prompt orchestration handled server-side

🔐 Configuration & Secrets

All sensitive values are handled via environment variables.

Example:

SURF_LLM_API_KEY=your_api_key_here


No secrets are committed to the repository.

▶️ Running the Project
Backend (local)
cd backend
mvn spring-boot:run

Backend (Docker)
docker-compose up --build


The API will be available at:

http://localhost:8080

📂 Project Structure (Backend)
backend/
├── controller/      # REST controllers
├── service/         # Business logic
├── repository/      # JPA repositories
├── entities/        # Domain models
├── dto/             # Data transfer objects
├── mappers/         # Entity ↔ DTO mapping
├── bootstrap/       # Dev data seeding
└── resources/
    └── application.properties

🎯 Project Goals

Practice production-style backend architecture

Integrate LLMs responsibly (no hardcoded keys, no vendor lock-in)

Build a realistic decision-support system, not a demo toy

Serve as a portfolio project for backend / AI-adjacent roles

🧠 Status

Backend core: ✅ implemented

LLM integration: ✅ functional

Forecast logic: ⚙️ evolving

Frontend: 🚧 in progress

Tests: 🚧 to be expanded

📜 License

This project is for educational and portfolio purposes.
