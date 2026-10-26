# UML & Architectural Diagrams
## Online Course Registration Management System

This document contains complete, standards-compliant UML and database architecture diagrams for the **Online Course Registration Management System**.

---

### 1. Use Case Diagram

```mermaid
flowchart LR
    subgraph Actors
        Student(("👤 Student"))
        Admin(("🛡️ Admin"))
    end

    subgraph "Online Course Registration Management System"
        UC1(["Register Account"])
        UC2(["Student Login"])
        UC3(["Browse & Search Courses"])
        UC4(["Filter Courses by Dept / Availability"])
        UC5(["View Course Details & Seats"])
        UC6(["Register for Course"])
        UC7(["View Enrolled Courses"])
        UC8(["Cancel Course Registration"])
        UC9(["View & Update Profile"])
        
        UC10(["Admin Login"])
        UC11(["View Dashboard & Capacity Stats"])
        UC12(["Add New Course Offering"])
        UC13(["Modify Course Details"])
        UC14(["Delete Course"])
        UC15(["Inspect Student Roster"])
        UC16(["Review All Registrations & Reports"])
        UC17(["Revoke Course Enrollment"])
    end

    Student --> UC1
    Student --> UC2
    Student --> UC3
    Student --> UC4
    Student --> UC5
    Student --> UC6
    Student --> UC7
    Student --> UC8
    Student --> UC9

    Admin --> UC10
    Admin --> UC11
    Admin --> UC12
    Admin --> UC13
    Admin --> UC14
    Admin --> UC15
    Admin --> UC16
    Admin --> UC17
```

---

### 2. Use Case Package Diagram

```mermaid
flowchart TD
    subgraph "Online Course Registration System"
        subgraph "Auth Package"
            P1["Student / Admin Authentication & Profile Management"]
        end

        subgraph "Course Catalog Package"
            P2["Course Browsing, Search, Department Filtering & Capacity Tracking"]
        end

        subgraph "Registration Package"
            P3["Course Enrollment, Duplicate Check, Cancellation & Seat Restoring"]
        end

        subgraph "Administration Package"
            P4["Course CRUD, Student Directory, Enrollment Ledger & System Analytics"]
        end
    end

    P1 --> P3
    P2 --> P3
    P4 --> P2
    P4 --> P3
```

---

### 3. Domain Class Diagram

```mermaid
classDiagram
    direction TB

    class Student {
        -Long studentId
        -String name
        -String email
        -String password
        -String rollNo
        -String department
        -Integer year
        -String phone
        +register()
        +login()
        +updateProfile()
    }

    class Admin {
        -Long adminId
        -String name
        -String email
        -String password
        +login()
        +getDashboardStats()
    }

    class Course {
        -Long courseId
        -String courseCode
        -String courseName
        -String description
        -String department
        -Integer credits
        -String facultyName
        -Integer maxSeats
        -Integer availableSeats
        -String duration
        -String status
        +decrementSeats()
        +incrementSeats()
        +isFull()
    }

    class Registration {
        -Long registrationId
        -LocalDateTime registrationDate
        -String status
        +confirmRegistration()
        +cancel()
    }

    Student "1" --> "0..*" Registration : enrolls_in
    Course "1" --> "0..*" Registration : assigned_to
```

---

### 4. High-Level Sequence Diagram (Student Course Registration)

```mermaid
sequenceDiagram
    autonumber
    actor Student
    participant Frontend as Web Browser (UI)
    participant Backend as Spring Boot REST API
    participant DB as MySQL Database

    Student->>Frontend: Click "Register Now" on Course Card
    Frontend->>Backend: POST /api/registrations (studentId, courseId)
    Backend->>DB: Query Student, Course & Existing Registrations
    DB-->>Backend: Return Records
    alt Seats Available & No Duplicate
        Backend->>DB: INSERT Registration & Decrement Course Available Seats
        DB-->>Backend: Commit Transaction
        Backend-->>Frontend: 201 Created ("Course registered successfully")
        Frontend-->>Student: Display Success Toast & Update Seat Badge
    else Duplicate Registration Or Course Full
        Backend-->>Frontend: 409 Conflict / 400 Bad Request (Error Details)
        Frontend-->>Student: Display Alert Modal with Reason
    end
```

---

### 5. Detailed Sequence Diagram (Course Registration with Seat Validation)

```mermaid
sequenceDiagram
    autonumber
    actor Student
    participant UI as Browser (courses.html)
    participant RC as RegistrationController
    participant RS as RegistrationServiceImpl
    participant SR as StudentRepository
    participant CR as CourseRepository
    participant RR as RegistrationRepository
    participant DB as MySQL Database

    Student->>UI: Select course and confirm registration
    UI->>RC: POST /api/registrations (RegistrationDTO)
    RC->>RS: registerCourse(registrationDTO)
    
    RS->>SR: findById(studentId)
    SR->>DB: SELECT * FROM student WHERE student_id = ?
    DB-->>SR: Student record
    SR-->>RS: Student entity (or 404 ResourceNotFoundException)

    RS->>CR: findById(courseId)
    CR->>DB: SELECT * FROM course WHERE course_id = ?
    DB-->>CR: Course record
    CR-->>RS: Course entity (or 404 ResourceNotFoundException)

    RS->>RR: findByStudentStudentIdAndCourseCourseId(studentId, courseId)
    RR->>DB: SELECT * FROM registration WHERE student_id = ? AND course_id = ?
    DB-->>RR: Record or empty

    alt Already registered with status 'REGISTERED'
        RS-->>RC: throw DuplicateRegistrationException
        RC-->>UI: 409 Conflict ("You are already registered for this course")
    else Course Available Seats <= 0
        RS-->>RC: throw CourseFullException
        RC-->>UI: 400 Bad Request ("Sorry, no seats are available")
    else Eligible
        Note over RS,CR: Decrement availableSeats by 1
        RS->>CR: save(course)
        CR->>DB: UPDATE course SET available_seats = available_seats - 1
        
        Note over RS,RR: Create Registration with status 'REGISTERED' & timestamp
        RS->>RR: save(registration)
        RR->>DB: INSERT INTO registration (...) VALUES (...)
        DB-->>RR: Saved registration
        
        RS-->>RC: RegistrationResponseDTO
        RC-->>UI: 201 Created (ApiResponse)
        UI-->>Student: Show Success Alert & Refresh Live Seats
    end
```

---

### 6. High-Level Communication Diagram (Course Registration Flow)

```mermaid
flowchart LR
    S(("Student")) -- "1: Select Course & Register" --> FE["Web Frontend"]
    FE -- "2: POST /api/registrations" --> CTRL["RegistrationController"]
    CTRL -- "3: registerCourse()" --> SVC["RegistrationService"]
    SVC -- "4: Validate & Update Seats" --> REPO["Data Repositories"]
    REPO -- "5: SQL INSERT & UPDATE" --> DB[("MySQL Database")]
```

---

### 7. Detailed Communication Diagram (Cancellation / Drop Course)

```mermaid
flowchart TD
    S(("Student / Admin")) -- "1: Click 'Drop Course'" --> FE["UI (my-courses.html)"]
    FE -- "2: DELETE /api/registrations/{id}" --> RC["RegistrationController"]
    RC -- "3: cancelRegistration(id)" --> RS["RegistrationServiceImpl"]
    RS -- "4.1: findById(id)" --> RR["RegistrationRepository"]
    RS -- "4.2: availableSeats + 1" --> CR["CourseRepository"]
    RS -- "4.3: delete(registration)" --> RR
    RR -- "5.1: DELETE FROM registration" --> DB[("MySQL DB")]
    CR -- "5.2: UPDATE course SET available_seats" --> DB
    RS -- "6: Success Confirmation" --> RC
    RC -- "7: 200 OK Response" --> FE
    FE -- "8: Refresh Enrolled List & Show Toast" --> S
```

---

### 8. Activity Diagram (Student Course Registration Flow)

```mermaid
flowchart TD
    Start([Start]) --> Login[Student Logs In]
    Login --> Browse[Browse Course Catalog]
    Browse --> SearchFilter[Search by Keyword / Filter by Department]
    SearchFilter --> SelectCourse[Select Desired Course]
    SelectCourse --> CheckDuplicate{Already Enrolled in this Course?}

    CheckDuplicate -- Yes --> DuplicateError[Display Error: 'Already Registered']
    DuplicateError --> Browse

    CheckDuplicate -- No --> CheckSeats{Available Seats > 0?}
    CheckSeats -- No --> FullError[Display Error: 'Course Seats are Full']
    FullError --> Browse

    CheckSeats -- Yes --> Confirm[Confirm Registration Dialog]
    Confirm --> Process[Deduct 1 Seat & Insert Registration Entry]
    Process --> SuccessToast[Display Success Message]
    SuccessToast --> MyCourses[Update 'My Registered Courses' & Credit Tally]
    MyCourses --> End([End])
```

---

### 9. Component Diagram

```mermaid
flowchart TD
    subgraph Presentation_Layer ["Presentation Layer (Client Browser)"]
        HTML["HTML5 Views (index, courses, my-courses, admin-dashboard)"]
        CSS["Bootstrap 5 CSS & Custom Styling"]
        JS["JavaScript API Client (main.js)"]
    end

    subgraph Backend_Application ["Spring Boot Application (Port 8080)"]
        subgraph REST_Controllers ["REST Controllers"]
        C1["StudentController"]
        C2["CourseController"]
        C3["RegistrationController"]
        C4["AdminController"]
        end

        subgraph Service_Layer ["Service Layer"]
        S1["StudentService"]
        S2["CourseService"]
        S3["RegistrationService"]
        S4["AdminService"]
        end

        subgraph Repository_Layer ["Spring Data JPA Repositories"]
        R1["StudentRepository"]
        R2["CourseRepository"]
        R3["RegistrationRepository"]
        R4["AdminRepository"]
        end

        subgraph Exception_Handling ["Exception & Security"]
        EX["GlobalExceptionHandler"]
        CORS["CorsConfig"]
        end
    end

    subgraph Persistence_Layer ["Data Persistence Layer"]
        MySQL[("MySQL Database Server\n(online_course_registration)")]
    end

    HTML --> JS
    JS -- "HTTP / JSON REST" --> REST_Controllers
    REST_Controllers --> Service_Layer
    Service_Layer --> Repository_Layer
    Repository_Layer --> MySQL
```

---

### 10. Deployment Diagram

```mermaid
flowchart TD
    subgraph Client_Device ["Student / Admin Client Device"]
        Browser["Modern Web Browser\n(Chrome, Firefox, Edge, Safari)\nHTML5 / CSS3 / JavaScript (Fetch API)"]
    end

    subgraph Application_Server ["Application Server (Localhost / College Server)"]
        direction TB
        JVM["Java Virtual Machine (OpenJDK 17+)"]
        SpringBoot["Embedded Tomcat Server (Port 8080)\nSpring Boot 3.2.x Runtime\n- REST Controllers\n- Service Layer\n- Hibernate ORM Engine"]
        StaticAssets["Static Web Resources\n(classpath:/static/)"]
        JVM --- SpringBoot
        JVM --- StaticAssets
    end

    subgraph Database_Server ["Database Server"]
        MySQL_DB[("MySQL 8.0+ Instance\nPort: 3306\nDatabase: online_course_registration\nTables: student, admin, course, registration")]
    end

    Browser -- "HTTP / REST (Port 8080)" --> SpringBoot
    SpringBoot -- "JDBC Connection Pool (HikariCP)\nTCP/IP (Port 3306)" --> MySQL_DB
```

---

### 11. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    STUDENT ||--o{ REGISTRATION : registers
    COURSE ||--o{ REGISTRATION : contains
    
    STUDENT {
        bigint student_id PK "AUTO_INCREMENT"
        varchar name "100"
        varchar email UK "100"
        varchar password "255"
        varchar roll_no UK "50"
        varchar department "50"
        int year "1..4"
        varchar phone "20"
    }

    ADMIN {
        bigint admin_id PK "AUTO_INCREMENT"
        varchar name "100"
        varchar email UK "100"
        varchar password "255"
    }

    COURSE {
        bigint course_id PK "AUTO_INCREMENT"
        varchar course_code UK "50"
        varchar course_name "150"
        text description
        varchar department "50"
        int credits
        varchar faculty_name "100"
        int max_seats
        int available_seats
        varchar duration "50"
        varchar status "20"
    }

    REGISTRATION {
        bigint registration_id PK "AUTO_INCREMENT"
        bigint student_id FK "References student(student_id)"
        bigint course_id FK "References course(course_id)"
        datetime registration_date
        varchar status "30"
    }
```

---

### 12. Project Layered Architecture Diagram

```mermaid
flowchart TD
    A["👤 Student / 🛡️ Admin"]
    B["🖥️ Responsive Frontend (HTML5, CSS3, Bootstrap 5, JavaScript)"]
    C["🌐 RESTful API Endpoints (/api/students, /api/courses, /api/registrations, /api/admin)"]
    D["🎮 Controller Layer (@RestController)"]
    E["⚙️ Service Layer (Business Rules & Transaction Management)"]
    F["📦 Repository Layer (Spring Data JPA)"]
    G["🔄 JPA / Hibernate ORM Layer"]
    H[("🗄️ MySQL Database (online_course_registration)")]

    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    F --> G
    G --> H
```

---

### 13. System Workflow Diagrams

#### Student Workflow
```mermaid
flowchart TD
    S1([Register Account]) --> S2[Login with Email & Password]
    S2 --> S3[Access Student Dashboard]
    S3 --> S4[Browse Available Course Catalog]
    S4 --> S5[Search by Name or Filter by Department]
    S5 --> S6[View Detailed Course Description & Seat Counts]
    S6 --> S7[Click 'Register Now']
    S7 --> S8{Seats Available and No Duplicate?}
    S8 -- Yes --> S9[System Decrements Seat & Records Registration]
    S9 --> S10[Confirmation Displayed & Added to 'My Courses']
    S8 -- No --> S11[Alert: Duplicate or Class Full]
    S11 --> S4
    S10 --> S12[Option to Drop Course & Restore Seat]
```

#### Admin Workflow
```mermaid
flowchart TD
    A1([Admin Login]) --> A2[Admin Console Dashboard]
    A2 --> A3[Monitor Real-Time Capacity & Enrollments]
    A3 --> A4[Manage Courses: Add, Edit, Delete, Toggle Status]
    A3 --> A5[Manage Students: View Directory & Registrations]
    A3 --> A6[View Master Registration Ledger & Reports]
    A3 --> A7[Revoke Registration if Required]
```
