# 📚 EduFlex DQDT — Project Context for AI Agent

> **Đây là file context dành cho AI Agent (VS Code Copilot / Cursor / Continue) đọc và hỗ trợ phát triển ứng dụng EduFlex DQDT.**
> Mọi suggestion, code generation, refactor đều phải dựa trên context này.

---

## 1. TỔNG QUAN DỰ ÁN

| Thông tin | Chi tiết |
|---|---|
| **Tên ứng dụng** | DQDT EduFlex |
| **Loại ứng dụng** | Android Native App |
| **Concept** | Kết hợp Coursera (khóa học có cấu trúc) + Duolingo (gamification, luyện tập hằng ngày) |
| **Thời gian phát triển** | 4 tuần (Sprint-based) |
| **Môi trường code** | VS Code + Android device (USB debug) |
| **Nhóm phát triển** | 4 thành viên — HCMUS, Khoa CNTT |

### Mục tiêu cốt lõi
- Cung cấp nền tảng học tập đa lĩnh vực (ngoại ngữ, kỹ năng, kiến thức nền tảng)
- Duy trì động lực học tập qua gamification (XP, level, streak, badge, leaderboard)
- Cá nhân hóa lộ trình học bằng AI
- Hỗ trợ 3 loại người dùng: Học viên, Giảng viên, Admin

---

## 2. KIẾN TRÚC & CÔNG NGHỆ

### 2.1 Android Frontend
- **Ngôn ngữ:** Java (KHÔNG dùng Kotlin)
- **Giao diện:** XML Layout (KHÔNG dùng Jetpack Compose)
- **Kiến trúc:** MVC (Model - View - Controller)
  - `Model`: POJO classes, Repository, API Service
  - `View`: Activity, Fragment, XML layout files
  - `Controller`: Activity/Fragment đóng vai trò controller, xử lý logic UI
- **HTTP Client:** Retrofit2 + OkHttp3
- **Image Loading:** Glide
- **Local Storage:** SharedPreferences (token, user session), Room Database (offline cache)
- **Navigation:** Intent-based + Fragment Transaction
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)

### 2.2 Backend (Spring Boot)
- **Framework:** Java Spring Boot 3.x
- **Modules:** Spring Web, Spring Data JPA, Spring Security
- **Authentication:** JWT (access token + refresh token) + Google OAuth 2.0
- **API Style:** RESTful API
- **Build Tool:** Maven

### 2.3 Database
- **RDBMS:** PostgreSQL
- **ORM:** Hibernate (via Spring Data JPA)
- **Admin Tool:** pgAdmin

### 2.4 Authentication
- Firebase Authentication (Email/Password)
- Google Sign-In (OAuth 2.0)
- JWT Token lưu tại client trong SharedPreferences

### 2.5 Dev Tools
- **IDE:** VS Code (Android extension pack)
- **Testing device:** Android physical device via USB (ADB)
- **API Test:** Postman
- **Version Control:** Git / GitHub
- **DB Admin:** pgAdmin

---

## 3. CẤU TRÚC THƯ MỤC DỰ ÁN

```
EduFlex/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/dqdt/eduflex/
│           │   ├── model/               # POJO / Entity classes
│           │   │   ├── User.java
│           │   │   ├── Course.java
│           │   │   ├── Lesson.java
│           │   │   ├── Quiz.java
│           │   │   ├── UserProgress.java
│           │   │   └── GamificationStats.java
│           │   ├── api/                 # Retrofit interfaces & responses
│           │   │   ├── ApiClient.java
│           │   │   ├── ApiService.java
│           │   │   └── response/
│           │   ├── repository/          # Data layer (calls API or Room)
│           │   │   ├── AuthRepository.java
│           │   │   ├── CourseRepository.java
│           │   │   └── UserRepository.java
│           │   ├── ui/                  # Activities & Fragments (View + Controller)
│           │   │   ├── auth/
│           │   │   │   ├── LoginActivity.java
│           │   │   │   └── RegisterActivity.java
│           │   │   ├── home/
│           │   │   │   ├── HomeActivity.java
│           │   │   │   └── HomeFragment.java
│           │   │   ├── course/
│           │   │   │   ├── CourseListFragment.java
│           │   │   │   ├── CourseDetailActivity.java
│           │   │   │   └── LessonActivity.java
│           │   │   ├── practice/
│           │   │   │   ├── DailyPracticeActivity.java
│           │   │   │   ├── QuizFragment.java
│           │   │   │   └── MatchingFragment.java
│           │   │   ├── profile/
│           │   │   │   ├── ProfileFragment.java
│           │   │   │   └── ProgressFragment.java
│           │   │   ├── leaderboard/
│           │   │   │   └── LeaderboardFragment.java
│           │   │   └── admin/
│           │   │       └── AdminDashboardActivity.java
│           │   ├── adapter/             # RecyclerView Adapters
│           │   │   ├── CourseAdapter.java
│           │   │   ├── LessonAdapter.java
│           │   │   └── LeaderboardAdapter.java
│           │   ├── utils/               # Helpers, constants
│           │   │   ├── TokenManager.java
│           │   │   ├── NetworkUtils.java
│           │   │   └── Constants.java
│           │   └── service/             # Notification, background tasks
│           │       └── ReminderService.java
│           └── res/
│               ├── layout/              # XML UI files
│               ├── drawable/
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── menu/
├── backend/ (Spring Boot project - separate repo/folder)
│   └── src/main/java/com/dqdt/eduflex/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       ├── dto/
│       ├── security/
│       └── config/
└── PROJECT_CONTEXT.md  ← (file này)
```

---

## 4. CÁC CHỨC NĂNG CHI TIẾT

### 4.1 Học viên (Learner)
| # | Chức năng | Màn hình liên quan | Độ ưu tiên |
|---|---|---|---|
| 1 | Đăng ký / Đăng nhập (Email + Google) | LoginActivity, RegisterActivity | 🔴 P1 |
| 2 | Quản lý hồ sơ & mục tiêu học tập | ProfileFragment | 🔴 P1 |
| 3 | Xem, tìm kiếm, lọc khóa học | CourseListFragment | 🔴 P1 |
| 4 | Xem chi tiết khóa học | CourseDetailActivity | 🔴 P1 |
| 5 | Thanh toán & tham gia khóa học | PaymentActivity | 🟡 P2 |
| 6 | Học bài giảng (video, text, ảnh) | LessonActivity | 🔴 P1 |
| 7 | Luyện tập hằng ngày (quiz, điền từ, ghép cặp) | DailyPracticeActivity | 🔴 P1 |
| 8 | Kiểm tra giữa khóa / cuối khóa | ExamActivity | 🟡 P2 |
| 9 | Theo dõi tiến độ học tập | ProgressFragment | 🔴 P1 |
| 10 | Nhắc nhở học tập qua notification | ReminderService | 🟡 P2 |
| 11 | Gamification (XP, level, streak, badge) | HomeFragment, ProfileFragment | 🔴 P1 |
| 12 | Bảng xếp hạng (Leaderboard) | LeaderboardFragment | 🟡 P2 |
| 13 | Đánh giá & phản hồi khóa học | CourseDetailActivity | 🟢 P3 |
| 14 | Nhận chứng chỉ hoàn thành | CertificateActivity | 🟢 P3 |

### 4.2 AI Features
| # | Chức năng | Ghi chú |
|---|---|---|
| 1 | Gợi ý lộ trình học phù hợp | Dựa trên tiến độ + kết quả quiz |
| 2 | Đề xuất bài ôn lại | Spaced repetition algorithm |

### 4.3 Giảng viên & Admin
| # | Chức năng |
|---|---|
| 1 | Tạo, chỉnh sửa, quản lý khóa học |
| 2 | Quản lý người dùng, phân quyền, thống kê |

---

## 5. KẾ HOẠCH 4 TUẦN (SPRINT PLAN)

### 🗓️ Tuần 1 — Foundation & Auth (19/03 – 25/03)
**Android:**
- [ ] Setup project, cấu hình Gradle, Retrofit, Glide
- [ ] Thiết kế màn hình Login / Register (XML)
- [ ] Tích hợp Firebase Authentication (Email/Password)
- [ ] Tích hợp Google Sign-In
- [ ] Lưu JWT token vào SharedPreferences (TokenManager)
- [ ] Bottom Navigation Bar (Home, Courses, Practice, Profile)
- [ ] Màn hình Home cơ bản (streak, XP, featured courses)

**Backend:**
- [ ] Setup Spring Boot project, PostgreSQL
- [ ] Tạo bảng User, Role trong DB
- [ ] API: POST /auth/register, POST /auth/login, POST /auth/google
- [ ] JWT filter & Spring Security config

---

### 🗓️ Tuần 2 — Courses & Learning (26/03 – 01/04)
**Android:**
- [ ] Màn hình danh sách khóa học (RecyclerView + filter/search)
- [ ] Màn hình chi tiết khóa học
- [ ] Màn hình học bài giảng (VideoView / WebView cho video, TextView cho text)
- [ ] Tích hợp API course, lesson
- [ ] Progress tracking UI (ProgressBar, % hoàn thành)

**Backend:**
- [ ] DB: Course, Lesson, Enrollment, LessonProgress
- [ ] API: GET /courses, GET /courses/{id}, GET /courses/{id}/lessons
- [ ] API: POST /enrollments, GET /enrollments/me
- [ ] API: PUT /progress/{lessonId}

---

### 🗓️ Tuần 3 — Gamification & Practice (02/04 – 08/04)
**Android:**
- [ ] Màn hình luyện tập hằng ngày
- [ ] Quiz Fragment (multiple choice, điền từ, ghép cặp)
- [ ] Hiển thị XP, level, streak, badge trên UI
- [ ] Leaderboard Fragment (RecyclerView + top 3 highlight)
- [ ] Push notification nhắc nhở học (AlarmManager + NotificationManager)
- [ ] Màn hình kết quả sau bài luyện tập

**Backend:**
- [ ] DB: Quiz, QuizQuestion, GamificationStats, Badge, LeaderboardEntry
- [ ] API: GET /practice/daily, POST /practice/submit
- [ ] API: GET /leaderboard
- [ ] API: GET /users/me/stats (XP, level, streak, badges)
- [ ] Logic tính XP và cập nhật streak

---

### 🗓️ Tuần 4 — AI, Polish & Testing (09/04 – 15/04)
**Android:**
- [ ] Màn hình Profile hoàn chỉnh (avatar, stats, badges)
- [ ] Tích hợp AI gợi ý lộ trình (hiển thị recommended courses)
- [ ] Màn hình Chứng chỉ (Certificate)
- [ ] Màn hình Exam (kiểm tra giữa/cuối khóa)
- [ ] UI polish: animation, transitions, loading states
- [ ] Test toàn bộ app trên thiết bị thực
- [ ] Fix bugs, edge cases

**Backend:**
- [ ] API: GET /ai/recommendations (rule-based hoặc simple ML)
- [ ] API: GET /certificates/{courseId}
- [ ] API: POST /exams/submit
- [ ] Performance tuning, error handling

---

## 6. DATABASE SCHEMA (Tóm tắt)

```sql
-- Users & Auth
users (id, email, name, avatar_url, role, created_at)
user_goals (id, user_id, goal_text, target_date)

-- Courses & Lessons
courses (id, title, description, thumbnail, category, price, instructor_id, created_at)
lessons (id, course_id, title, type[video/text/quiz], content_url, order_index, duration)
enrollments (id, user_id, course_id, enrolled_at, completed_at)
lesson_progress (id, user_id, lesson_id, completed, completed_at)

-- Practice & Quizzes
quizzes (id, lesson_id, type[multiple_choice/fill_blank/matching])
quiz_questions (id, quiz_id, question_text, correct_answer, options_json)
daily_practice (id, user_id, date, questions_json, score, completed)

-- Gamification
gamification_stats (id, user_id, xp, level, streak_days, last_study_date)
badges (id, name, description, icon_url, condition)
user_badges (id, user_id, badge_id, earned_at)

-- Ratings & Certificates
course_ratings (id, user_id, course_id, rating, comment, created_at)
certificates (id, user_id, course_id, issued_at, certificate_url)
```

---

## 7. API ENDPOINTS (Chính)

```
# Auth
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/google
POST   /api/auth/refresh-token

# Users
GET    /api/users/me
PUT    /api/users/me
GET    /api/users/me/stats

# Courses
GET    /api/courses?category=&search=&page=
GET    /api/courses/{id}
GET    /api/courses/{id}/lessons
POST   /api/courses          (Instructor only)
PUT    /api/courses/{id}     (Instructor only)

# Enrollments & Progress
POST   /api/enrollments
GET    /api/enrollments/me
PUT    /api/progress/lesson/{lessonId}

# Practice
GET    /api/practice/daily
POST   /api/practice/submit

# Gamification
GET    /api/leaderboard
GET    /api/badges
GET    /api/users/me/badges

# AI
GET    /api/ai/recommendations

# Certificates
GET    /api/certificates/me
GET    /api/certificates/{courseId}
```

---

## 8. CODING CONVENTIONS

### Android (Java)
- Package: `com.dqdt.eduflex`
- Class naming: PascalCase (`CourseDetailActivity`, `CourseAdapter`)
- Method naming: camelCase (`loadCourseData()`, `setupRecyclerView()`)
- Constants: UPPER_SNAKE_CASE trong `Constants.java`
- Layout file naming: `activity_*`, `fragment_*`, `item_*`, `dialog_*`
- Mỗi Activity chỉ có 1 nhiệm vụ chính (Single Responsibility)
- Dùng `AsyncTask` hoặc `Retrofit Callback` cho network calls (KHÔNG block UI thread)
- Tất cả String hiển thị UI phải khai báo trong `strings.xml`
- Tất cả màu sắc phải khai báo trong `colors.xml`

### Backend (Spring Boot)
- Package: `com.dqdt.eduflex`
- Controller chỉ nhận request, gọi Service
- Service chứa business logic
- Repository chỉ làm việc với DB
- Dùng DTO cho request/response (KHÔNG expose Entity trực tiếp)
- Exception handling tập trung tại `@ControllerAdvice`

---

## 9. SETUP MÔI TRƯỜNG (VS Code)

### Extensions cần thiết
- **Extension Pack for Java** (Microsoft)
- **Android iOS Emulator** hoặc dùng ADB trực tiếp
- **Gradle for Java**
- **XML** (Red Hat)
- **REST Client** (thay thế Postman trong VS Code)
- **GitLens**

### Kết nối thiết bị thực
```bash
# Bật USB Debugging trên điện thoại
# Developer Options > USB Debugging > ON

# Kiểm tra device đã connect
adb devices

# Build và deploy lên device
./gradlew installDebug

# Xem logcat
adb logcat | grep "EduFlex"
```

### Chạy Backend
```bash
cd backend
./mvnw spring-boot:run
# Backend chạy tại: http://localhost:8080
# Cấu hình Android gọi tới: http://10.0.2.2:8080 (emulator) 
# hoặc http://<IP_máy_tính>:8080 (physical device cùng WiFi)
```

---

## 10. PHÂN TÍCH RỦI RO & LƯU Ý

| Rủi ro | Giải pháp |
|---|---|
| 4 tuần là rất ngắn cho full-stack app | Ưu tiên P1 trước, P2/P3 nếu còn thời gian |
| Tích hợp Payment phức tạp | Mock payment flow cho demo, tích hợp thật sau |
| AI recommendation phức tạp | Dùng rule-based đơn giản (top rated + chưa học) thay vì ML |
| Video streaming tốn bandwidth | Dùng YouTube embed hoặc pre-uploaded URL thay vì tự host |
| JWT token expiry trên mobile | Implement auto refresh token trong Retrofit interceptor |
| Physical device test | Đảm bảo Backend và Device cùng mạng LAN khi test |

---

## 11. LUỒNG NGƯỜI DÙNG CHÍNH (User Flows)

### Flow 1: Onboarding
`Splash → Login/Register → Set Learning Goal → Home`

### Flow 2: Học khóa học
`Home → Course List → Course Detail → Enroll → Lesson 1 → ... → Complete → Certificate`

### Flow 3: Luyện tập hằng ngày
`Home (streak reminder) → Daily Practice → Quiz/Fill/Match → Result (+XP) → Updated Streak`

### Flow 4: Gamification
`Any action → +XP → Level up? → Badge earned? → Leaderboard position update`

---

## 12. DESIGN SYSTEM

### Color Palette
```xml
<!-- colors.xml -->
<color name="primary">#4F46E5</color>        <!-- Indigo - main brand -->
<color name="primary_dark">#3730A3</color>
<color name="accent">#F59E0B</color>          <!-- Amber - XP, streak -->
<color name="success">#10B981</color>         <!-- Green - completed -->
<color name="error">#EF4444</color>           <!-- Red - wrong answer -->
<color name="background">#F9FAFB</color>
<color name="surface">#FFFFFF</color>
<color name="text_primary">#111827</color>
<color name="text_secondary">#6B7280</color>
<color name="streak_orange">#F97316</color>   <!-- Streak color như Duolingo -->
```

### Typography
- Title: 20sp Bold
- Subtitle: 16sp Medium  
- Body: 14sp Regular
- Caption: 12sp Regular
- Font: Roboto (default Android)

---

*File này được tạo tự động từ Proposal "DQDT EduFlex" — HCMUS CNTT*
*Cập nhật lần cuối: 19/03/2026*
