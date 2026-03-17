EduFlex is a mobile learning app that offers flexible and personalized courses with clear learning paths. It combines interactive lessons, quizzes, and gamification features such as points, levels, and streaks to motivate users. Basic AI support helps suggest suitable learning content and improve learning efficiency.

# Project structure
- backend/: Spring Boot backend
- app/: Android app module

# Setup
- Create backend/.env from backend/.env.example
- Fill in Supabase keys in backend/.env

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
