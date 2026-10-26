# ONLINE COURSE REGISTRATION MANAGEMENT SYSTEM
### A Full-Stack College Academic Project Built with Java 17+, Spring Boot 3.2.4 & MySQL 8+

---

## 📌 1. Project Objective & Architecture

The **Online Course Registration Management System** is a production-style, transaction-safe academic management platform designed for universities and engineering colleges. It implements automated student course registrations with real-time seat tracking, duplicate registration prevention, department-wise elective browsing, transparent credit management, and comprehensive administrative controls.

### 🏛️ Complete Academic Lifecycle:
```text
Admin Configuration
        ↓
Branch (AIML, CSE, ECE)
        ↓
Minor Stream (Fixed from 2nd Year, e.g., AIML MAD Stream, Network Analysis)
        ↓
Sections (e.g., MAD Stream-1, MAD Stream-2 with section-level faculty assignment)
        ↓
Faculty Assignment (Dedicated faculty mentors per section and course instructors)
        ↓
Courses (Mapped strictly to Branch, Minor Stream, Semester & Faculty)
        ↓
Semester (Semesters 3 through 8 for Years 2 to 4)
        ↓
Registration Window (Start & End Deadlines with Live Status Checks)
        ↓
Eligible Student (Backend Eligibility: Branch + Fixed Minor Stream + Semester)
        ↓
Course Registration (Atomic 6-Step Transactional Execution)
        ↓
Seat Inventory Update (Decrement on Register / Increment on Drop)
        ↓
Registration History & Audit Trail
```

---

## 💻 2. Technology Stack

| Layer | Component | Version / Details |
|---|---|---|
| **Backend Framework** | Spring Boot | 3.2.4 |
| **Language** | Java | 17+ (Compiled on JDK 26 compatible target Java 17) |
| **Persistence / ORM** | Spring Data JPA / Hibernate | 6.x |
| **Database** | MySQL | 8.0+ |
| **Security** | Spring Security Crypto | BCrypt Password Hashing (`$2a$10$...`) |
| **AOP / Auditing** | Spring AOP / AspectJ | `@Aspect`, `@Around`, `@Before`, `@AfterThrowing` |
| **Transaction Management**| Spring `@Transactional` | Atomicity, Isolation, Automatic Rollback |
| **Validation** | Jakarta Bean Validation | `@NotBlank`, `@NotNull`, `@Email`, `@Size` |
| **Build Tool** | Apache Maven | 3.9+ |
| **Testing** | JUnit 5 & Mockito | Service layer unit tests with mocks |
| **Frontend UI** | HTML5, CSS3, Bootstrap 5.3.2 | Responsive single-page dashboards |
| **Alerts & Modals** | SweetAlert2, FontAwesome 6.5 | Interactive confirmations and notifications |
| **Architecture** | RESTful Web Services | JSON payloads with `@RestControllerAdvice` |

---

## 🎯 3. Syllabus Concepts Applied in Project

### A. Spring IoC & Dependency Injection:
* **Constructor Injection:** 100% of services, controllers, and aspects use constructor injection instead of artificial field injection.
* **Component Scanning:** Clear stereotyping using `@Component`, `@Service`, `@Repository`, and `@RestController`.

### B. Spring Boot Auto-Configuration:
* Clean modular configuration in `application.properties` with zero boilerplate XML.
* Custom `SecurityConfig` declaring the `BCryptPasswordEncoder` bean.

### C. Logging & Spring AOP:
* Centralized `LoggingAspect` cross-cutting the service layer:
  * `@Around("execution(* com.example.onlinecourse.service.RegistrationService.*(..))")` to measure registration execution latency.
  * `@Before` and `@AfterReturning` / `@AfterThrowing` for audit logs.
  * Password privacy: passwords are never logged.

### D. Spring Data JPA & Derived Queries:
* Rich entity model with `@ManyToOne`, `@OneToMany`, `@JoinColumn`, `@UniqueConstraint`.
* Custom JPQL queries in repositories:
  * `findEligibleCourses(...)` matching Student Branch, Minor Stream, and Semester.
  * `findStudentsByAssignedFacultyId(...)` enforcing section-level faculty privacy.
  * `isWindowOpen(...)` checking deadline bounds against `LocalDateTime.now()`.

### E. Pagination & Sorting:
* `Pageable` and `Sort` integrated in controllers and repositories for large datasets (`/api/students/page`, `/api/courses/page`, `/api/registrations/page`).

### F. Spring Transaction Management (`@Transactional`):
* **Course Registration 6-Step Atomic Lifecycle:**
  1. Active Course Check
  2. Registration Window Deadline Check
  3. Student Branch + Fixed Minor Stream + Semester Eligibility Check
  4. Duplicate Registration Verification
  5. Seat Availability Check (`availableSeats > 0`)
  6. Atomic Registration Creation & Seat Inventory Decrement (`availableSeats - 1`)
* **Course Drop Atomicity:**
  * Ownership verification (`registration.studentId == currentStudentId`).
  * Atomic status transition and seat inventory increment (`availableSeats + 1`).

### G. Centralized Exception Handling:
* `@RestControllerAdvice` in `GlobalExceptionHandler` with targeted handlers:
  * `RegistrationClosedException` (400 Bad Request)
  * `InvalidCourseEligibilityException` (400 Bad Request)
  * `DuplicateRegistrationException` (409 Conflict)
  * `CourseFullException` (409 Conflict)
  * `ResourceNotFoundException` / `StudentNotFoundException` (404 Not Found)
  * `UnauthorizedOperationException` (403 Forbidden)
  * `MethodArgumentNotValidException` (DTO validation errors)

---

## 🔒 4. Core Business Requirements Enforced

1. **Fixed Minor Stream (Second Year onwards):**
   * Students choose their minor stream in 2nd year. Once assigned, **it is permanent and locked**.
   * Students **CANNOT** alter their minor stream or branch. The student UI displays a locked badge and no "Change Minor Stream" option exists.
   * `StudentServiceImpl.updateStudentSelfProfile()` throws `UnauthorizedOperationException` if a student attempts to modify branch or minor stream.
2. **Multiple Sections per Minor Stream:**
   * Minor streams support multiple sections (e.g. `MAD Stream-1`, `MAD Stream-2`).
   * Administrators assign separate faculty members to sections (`/api/sections/{id}/assign-faculty/{facultyId}`).
3. **Faculty Role-Based Privacy Restriction:**
   * Faculty members **can ONLY view students belonging to their assigned sections** (`/api/faculty/{id}/students`).
4. **Strict Registration Windows:**
   * Courses can only be registered or dropped while the semester's registration window is **OPEN**.

---

## 👥 5. Default Demonstration Credentials

All accounts are pre-seeded automatically by `DataInitializer`:

| Role | Username / Email | Password | Details |
|---|---|---|---|
| **Admin** | `admin@college.edu` | `admin123` | Full Administrative Master Access |
| **Student (AIML)** | `akhil@gmail.com` | `12345` | Roll: `22A01`, Minor: `AIML MAD Stream`, Sec: `MAD Stream-1`, Sem: 3 |
| **Student (AIML)** | `sneha@gmail.com` | `12345` | Roll: `22A14`, Minor: `AIML NA Stream`, Sec: `NA Stream-1`, Sem: 5 |
| **Student (CSE)** | `rahul@gmail.com` | `12345` | Roll: `22C05`, Minor: `CSE WD Stream`, Sec: `WD Stream-1`, Sem: 3 |
| **Student (AIML)** | `pooja@gmail.com` | `12345` | Roll: `21A09`, Minor: `AIML CA Stream`, Sec: `CA Stream-1`, Sem: 5 |
| **Student (ECE)** | `karan@gmail.com` | `12345` | Roll: `22E32`, Minor: `ECE VLSI Stream`, Sec: `VLSI Stream-1`, Sem: 3 |
| **Faculty (AIML)** | `ravi.kumar@college.edu` | `faculty123` | Code: `FAC001`, Professor, Assigned to `MAD Stream-1` & `NA Stream-1` |
| **Faculty (AIML)** | `priya.sharma@college.edu` | `faculty123` | Code: `FAC002`, Assoc. Prof, Assigned to `MAD Stream-2` & `CA Stream-1` |
| **Faculty (CSE)** | `rajesh.verma@college.edu` | `faculty123` | Code: `FAC003`, Asst. Prof, Assigned to `WD Stream-1` |
| **Faculty (ECE)** | `ananya.sen@college.edu` | `faculty123` | Code: `FAC004`, Professor, Assigned to `VLSI Stream-1` |
| **Faculty (CSE)** | `vikram.malhotra@college.edu`| `faculty123`| Code: `FAC005`, Assoc. Prof |

---

## 🚀 6. Setup & Running Instructions

### Prerequisites:
* MySQL 8.0+ running on `localhost:3306` (Password supplied through the `DB_PASSWORD` environment variable)
* Java 17+ installed (`JAVA_HOME` pointing to JDK)
* Apache Maven

### Step 1: Clone or Navigate to Project Directory
```powershell
cd D:\API_PROJECT
```

### Step 2: Build the Application
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-26.0.1"
mvn clean package
```

### Step 3: Run the Application
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-26.0.1"
mvn spring-boot:run
```
*(Or execute the generated executable JAR directly:)*
```powershell
java -jar target/online-course-registration-1.0.0.jar
```

### Step 4: Open in Web Browser
Open your browser and navigate to:
```text
http://localhost:8080/index.html
```

---

## 🌐 7. Key REST API Endpoints

### 🔐 Authentication:
* `POST /api/admin/login` - Administrator login
* `POST /api/admin/faculty/login` - Faculty login
* `POST /api/students/login` - Student login

### 🏛️ Branches & Minor Streams:
* `GET /api/branches` - List all branches
* `POST /api/branches` - Create branch
* `GET /api/minor-streams` - List minor streams
* `GET /api/minor-streams/branch/{branchId}` - Minor streams filtered by branch
* `POST /api/minor-streams` - Create minor stream

### 👥 Sections & Faculty:
* `GET /api/sections` - List sections
* `POST /api/sections` - Create section
* `PUT /api/sections/{id}/assign-faculty/{facultyId}` - Assign faculty mentor to section
* `GET /api/faculty` - List faculty
* `GET /api/faculty/{id}/sections` - Sections assigned to faculty
* `GET /api/faculty/{id}/courses` - Courses taught by faculty
* `GET /api/faculty/{id}/students` - Enrolled students (role-restricted to assigned sections)

### 🗓️ Semesters & Windows:
* `GET /api/semesters` - List semesters (Semesters 3 through 8)
* `GET /api/registration-windows` - List registration windows
* `GET /api/registration-windows/semester/{semesterId}/open` - Live boolean window open status
* `POST /api/registration-windows` - Configure new registration window with deadline

### 📚 Courses & Student Registration:
* `GET /api/courses` - Master course catalog
* `POST /api/courses` - Create new course offering
* `GET /api/students/{id}/eligible-courses` - Eligible courses for student (Branch + Minor Stream + Semester)
* `POST /api/registrations` - Register course (atomic 6-step transactional execution)
* `DELETE /api/registrations/{id}/drop/{studentId}` - Drop course (atomic seat inventory increment)
* `GET /api/students/{id}/registrations` - Student's enrolled courses and history


## Requirement Coverage (Finalized)

The implementation enforces the following project-specific rules:
- Branch and minor stream are part of the student's fixed academic identity; students cannot change them through self-service APIs.
- A branch may contain multiple minor streams (for example AIML MAD, Network Analysis, and Conjugate Analysis).
- Eligible courses are calculated on the backend using branch + fixed minor stream + current semester + active course status.
- Registration is allowed only during an administrator-controlled registration window. The backend rejects expired/closed windows even when called directly through Postman.
- Section-level faculty assignment means all students in a section share that section's faculty. Faculty access is limited to students in assigned sections.
- Faculty selection is enabled for a minor stream only after it has more than two sections; the seeded AIML MAD stream demonstrates MAD Stream-1, MAD Stream-2, and MAD Stream-3.
- Students cannot list, modify, or delete other students; students can only access their own profile, eligible courses, and registrations.
- Students can drop only their own registrations. Administrative cancellation of registrations is restricted to ADMIN.
- JWT secrets and database passwords are supplied through environment variables (`JWT_SECRET`, `DB_PASSWORD`) and are not stored in source code.

### Eclipse execution
1. Start MySQL.
2. Run `OnlineCourseApplication` using the saved Eclipse Run Configuration.
3. Ensure `DB_PASSWORD` and `JWT_SECRET` are configured in that Run Configuration.
4. Open `http://localhost:8080/`.
