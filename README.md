EduFlex is a mobile learning app that offers flexible and personalized courses with clear learning paths. It combines interactive lessons, quizzes, and gamification features such as points, levels, and streaks to motivate users. Basic AI support helps suggest suitable learning content and improve learning efficiency.

# Project structure
- backend/: Spring Boot backend
- app/: Android app module

# Setup
- Copy `backend/.env.example` to `backend/.env`; set a strong JWT secret and optional Supabase/Gemini keys.
- Copy `.env.example` to `.env`; set the Android API base URL.
- Docker supplies PostgreSQL + pgvector, Redis, and RabbitMQ locally, so no remote database is required.

# Run backend
- cd backend
- ./mvnw spring-boot:run

# Run Android (Android Studio)
- Open this repository root
- Wait for Gradle sync
- Run app on emulator/device

# Run Android (CLI)

Build APK:
- ./gradlew --no-daemon :app:assembleDebug  

Need an emulator/device connected for install:
- ./gradlew --no-daemon :app:installDebug 

# CLI troubleshooting
- Lock issue: ./gradlew --stop
- Retry with isolated cache:
	- GRADLE_USER_HOME=/tmp/eduflex-gradle-home ./gradlew --no-daemon :app:assembleDebug

# Supabase health check
- GET http://localhost:8080/api/supabase/health

## Architecture upgrades

- Redis caches the course catalog, semantic search results, and generated course summaries with purpose-specific TTLs.
- RabbitMQ receives post-commit course/lesson change events and refreshes pgvector embeddings asynchronously.
- Grounded AI runs in the backend. `GET /api/course/{courseId}/ai-summary` summarizes stored course material, while `POST /api/course/{courseId}/ask` retrieves relevant lesson chunks before answering and returns its sources.
- Android provider keys were removed from `BuildConfig`; the mobile app only calls authenticated EduFlex APIs.
- Admin routes require an `ADMIN` role carried in the signed JWT.

## Local infrastructure

```bash
cp backend/.env.example backend/.env
cp .env.example .env
docker compose -f backend/docker-compose.yml up --build
```

Set a strong Redis password, RabbitMQ password, JWT secret, and local PostgreSQL password in `backend/.env`. `GEMINI_API_KEY` is optional: without it, summaries return a deterministic course overview and Q&A returns retrieved lesson suggestions.

Normal Maven builds do not connect to the database for jOOQ generation:

```bash
cd backend
./mvnw test
```

After a schema change, regenerate jOOQ sources explicitly with database environment variables configured:

```bash
./mvnw -Djooq.codegen.skip=false generate-sources
```

Android builds require JDK 17 or 21 and a configured Android SDK (`sdk.dir` in untracked `local.properties` or the standard SDK environment setting).
