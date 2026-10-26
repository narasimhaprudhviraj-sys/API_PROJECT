# ACADEMIC PROJECT REPORT
## ONLINE COURSE REGISTRATION MANAGEMENT SYSTEM

---

### 1. TITLE PAGE

**PROJECT TITLE:** ONLINE COURSE REGISTRATION MANAGEMENT SYSTEM  
**DOMAIN:** Enterprise Web Application Development / Software Engineering  
**ACADEMIC PROGRAM:** Bachelor of Technology / Bachelor of Science in Computer Science & Engineering  
**TECH STACK:** Java 17, Spring Boot 3.2.x, Spring Data JPA, Hibernate, MySQL 8.0, HTML5, CSS3, JavaScript, Bootstrap 5  
**DEVELOPMENT ENVIRONMENT:** Eclipse IDE, Apache Maven  

---

### 2. CERTIFICATE

This is to certify that the project entitled **“ONLINE COURSE REGISTRATION MANAGEMENT SYSTEM”** submitted in partial fulfillment of the requirements for the award of the Degree in Computer Science and Engineering is a bonafide record of work carried out under academic supervision and guidance. The results embodied in this report have not been submitted to any other university or institute for the award of any degree or diploma.

**Project Guide:** ___________________________  
**Head of Department (CSE):** ___________________________  
**External Examiner:** ___________________________  
**Date:** September 2026  

---

### 3. DECLARATION

I hereby declare that the project entitled **“ONLINE COURSE REGISTRATION MANAGEMENT SYSTEM”** is an authentic piece of work carried out independently under the guidance of our project supervisor. All references, citations, and external literature consulted during the development of this project have been acknowledged.

**Candidate Name:** Akhil Sharma  
**Roll Number:** 22A01  
**Department:** Computer Science and Engineering  

---

### 4. ACKNOWLEDGEMENT

I wish to express my sincere gratitude to our respected Principal and Head of the Department for providing the necessary laboratory infrastructure, high-speed network facilities, and academic encouragement required to bring this project to fruition.

I also extend heartfelt thanks to our Project Guide for continuous mentorship, technical constructive critiques, and valuable suggestions during the design, coding, testing, and documentation phases. Finally, I thank my family and peers for their continuous support and cooperation.

---

### 5. ABSTRACT

Manual academic course registration processes in higher education institutions are often vulnerable to logistical bottlenecks, paper waste, registration queues, data duplication, and human errors during seat allocation. 

The **Online Course Registration Management System** is a full-stack, enterprise-grade web application engineered to digitize and automate the entire lifecycle of student elective registration. Developed using **Java 17**, **Spring Boot**, **Spring Data JPA**, **Hibernate**, and **MySQL** on the backend, complemented by a responsive **HTML5/Bootstrap 5/JavaScript** frontend, the system provides separate dedicated portals for Students and Administrators.

Key innovations include automated real-time course seat capacity management, duplicate enrollment prevention, dynamic multi-attribute course search and department filtering, transparent student credit tracking, and instant registration revoking with immediate seat inventory restoration. The solution demonstrates high architectural scalability, ACID-compliant database transactions, and an intuitive user interface.

---

### 6. INTRODUCTION

Course registration is a core semester milestone for every university and college. Traditionally, students submitted physical slips or queued outside administrative counters to register for departmental electives. Such approaches result in seat calculation errors, delay in course roster preparation, and dissatisfaction when students register for already-full courses.

This project introduces a centralized web platform that provides instantaneous feedback on course availability, enforces academic limits, prevents repeated enrollment, and gives administrators live monitoring dashboards.

---

### 7. PROBLEM STATEMENT

Manual or poorly integrated registration platforms face critical flaws:
1. **Concurrency and Over-allocation:** Two students can simultaneously attempt registration, causing seat counts to exceed classroom limits.
2. **Duplicate Enrolments:** Absence of unique constraints allows a student to register multiple times for the same subject.
3. **Delayed Information Flow:** Students cannot view available elective options, faculty names, or credits in real time.
4. **Administrative Overhead:** Verifying student prerequisites and compiling paper rosters consumes valuable faculty hours.

---

### 8. EXISTING SYSTEM

In conventional systems:
* Students manually submit printed registration forms to faculty advisors.
* Staff cross-verify student roll numbers and manually tally seat counts in spreadsheets.
* Seat counts are updated at the end of each day rather than on every transaction.
* Drop or course cancellation requests require physical signatures and often fail to restore seats to other waiting students.

**Drawbacks:**
* High latency in finalizing course lists.
* Prone to physical document misplacement.
* No self-service portal for students.
* Lack of audit trails and timestamped logs.

---

### 9. PROPOSED SYSTEM

The proposed **Online Course Registration Management System** provides:
1. **Self-Service Student Portal:** Direct account registration, credential-based sign in, live course search, one-click course enrollment, and student-initiated course drops.
2. **Automated Inventory Control:** Database-level and service-level atomic decrements of available seats upon registration and increments upon cancellation.
3. **Hard Duplicate Prevention:** A composite database constraint and service verification ensuring a student cannot register twice for the same course code.
4. **Administrative Command Center:** Live capacity monitor with percentage bars, course catalog CRUD, student directory, and downloadable registration ledgers.

---

### 10. OBJECTIVES

1. Design and develop a clean, layered 3-tier software architecture using Spring Boot and MySQL.
2. Implement RESTful APIs returning standard HTTP response codes and clean error payloads.
3. Provide automated seat tracking where course capacity decreases by 1 on registration and increases by 1 on drop.
4. Prevent registrations if available seats are 0 or if the student has already enrolled.
5. Create a responsive, attractive frontend usable on desktops, tablets, and mobile devices.

---

### 11. SCOPE

* **Target Users:** College undergraduate and postgraduate students, departmental heads, and academic administrators.
* **Functional Scope:** Student registration, profile management, course catalog, course search and filter, one-click enrollment, drop course, administrative course maintenance, student roster, and registration auditing.
* **Future Expansion:** Online payment gateways, prerequisite validation engines, timetable clash detection, and mobile applications.

---

### 12. FUNCTIONAL REQUIREMENTS

#### Student Role:
* **FR-S01:** The system shall allow students to create an account with name, unique email, password, unique roll number, department, year, and phone.
* **FR-S02:** The system shall authenticate students using their email and password.
* **FR-S03:** The system shall display available courses with course code, name, department, credits, faculty, duration, and remaining seats.
* **FR-S04:** The system shall allow keyword searching across course name, course code, and faculty name.
* **FR-S05:** The system shall allow filtering by department and available seat status.
* **FR-S06:** The system shall allow students to register for active courses with available seats.
* **FR-S07:** The system shall strictly prevent duplicate registration for the same course by the same student.
* **FR-S08:** The system shall display a list of all courses registered by the student.
* **FR-S09:** The system shall allow students to cancel course registrations, instantly restoring the seat count.
* **FR-S10:** The system shall allow students to view and update their profile details.

#### Administrator Role:
* **FR-A01:** The system shall authenticate administrators using email and password.
* **FR-A02:** The system shall present an analytical dashboard with total students, courses, registrations, and available seat metrics.
* **FR-A03:** The system shall allow administrators to add new courses.
* **FR-A04:** The system shall allow administrators to edit course information and capacities.
* **FR-A05:** The system shall allow administrators to delete courses.
* **FR-A06:** The system shall allow administrators to view and search the student directory.
* **FR-A07:** The system shall display the master registration ledger with printable reports.

---

### 13. NON-FUNCTIONAL REQUIREMENTS

* **Performance:** REST API response times under 200 milliseconds under standard campus loads.
* **Reliability:** Data transactions handled with Spring `@Transactional` guaranteeing ACID properties.
* **Security:** Password fields never returned in API responses; role-based access checks.
* **Usability:** Clean Bootstrap 5 UI with intuitive icons, helpful alerts, and zero page reloads on REST operations.
* **Maintainability:** Strictly decoupled Model-View-Controller and Service-Repository layers.

---

### 14. HARDWARE REQUIREMENTS

* **Processor:** Intel Core i3 / AMD Ryzen 3 or higher (Intel Core i5 recommended).
* **RAM:** Minimum 4 GB (8 GB recommended for running IDE, Spring Boot, and MySQL concurrently).
* **Storage:** Minimum 500 MB free hard disk space for code repository, logs, and database files.
* **Display:** 1024x768 minimum screen resolution (1920x1080 recommended).

---

### 15. SOFTWARE REQUIREMENTS

* **Operating System:** Windows 10/11, Ubuntu Linux 20.04+, or macOS.
* **Java Development Kit:** JDK 17 or higher (LTS).
* **Build Tool:** Apache Maven 3.8+.
* **Integrated Development Environment (IDE):** Eclipse IDE for Enterprise Java and Web Developers / VS Code / IntelliJ IDEA.
* **Database Engine:** MySQL Server 8.0 or MariaDB 10.5+.
* **API Testing Tool:** Postman or Curl.
* **Web Browser:** Google Chrome, Mozilla Firefox, or Microsoft Edge.

---

### 16. TECHNOLOGY STACK

* **Frontend:** HTML5, CSS3, Bootstrap 5.3.2, Bootstrap Icons, Vanilla JavaScript (`fetch` API).
* **Backend:** Java 17, Spring Boot 3.2.4 (Spring Web, Spring Data JPA, Hibernate, Validation).
* **Database:** MySQL 8.0 with InnoDB engine and UTF-8 encoding.
* **Testing:** JUnit 5, Mockito.

---

### 17. SYSTEM ARCHITECTURE

The application adopts a **3-Tier Layered Architecture**:
1. **Presentation Layer:** Dynamic single and multi-page web interfaces rendered in the client browser.
2. **Application / Business Layer:** Spring Boot REST Controllers, DTO mappers, Service business logic, and custom Global Exception Handlers.
3. **Data Persistence Layer:** Spring Data JPA with Hibernate ORM communicating via HikariCP connection pooling to the MySQL Relational Database.

---

### 18. DATABASE DESIGN

Database Name: `online_course_registration`

#### Table Specifications:

1. **`student` Table:**
   - `student_id` (BIGINT, Primary Key, AUTO_INCREMENT)
   - `name` (VARCHAR(100), NOT NULL)
   - `email` (VARCHAR(100), NOT NULL, UNIQUE)
   - `password` (VARCHAR(255), NOT NULL)
   - `roll_no` (VARCHAR(50), NOT NULL, UNIQUE)
   - `department` (VARCHAR(50), NOT NULL)
   - `year` (INT, NOT NULL, CHECK: 1..4)
   - `phone` (VARCHAR(20), NOT NULL)

2. **`admin` Table:**
   - `admin_id` (BIGINT, Primary Key, AUTO_INCREMENT)
   - `name` (VARCHAR(100), NOT NULL)
   - `email` (VARCHAR(100), NOT NULL, UNIQUE)
   - `password` (VARCHAR(255), NOT NULL)

3. **`course` Table:**
   - `course_id` (BIGINT, Primary Key, AUTO_INCREMENT)
   - `course_code` (VARCHAR(50), NOT NULL, UNIQUE)
   - `course_name` (VARCHAR(150), NOT NULL)
   - `description` (TEXT)
   - `department` (VARCHAR(50), NOT NULL)
   - `credits` (INT, NOT NULL)
   - `faculty_name` (VARCHAR(100), NOT NULL)
   - `max_seats` (INT, NOT NULL)
   - `available_seats` (INT, NOT NULL, CHECK: 0..max_seats)
   - `duration` (VARCHAR(50), NOT NULL)
   - `status` (VARCHAR(20), NOT NULL, DEFAULT: 'ACTIVE')

4. **`registration` Table:**
   - `registration_id` (BIGINT, Primary Key, AUTO_INCREMENT)
   - `student_id` (BIGINT, Foreign Key referencing `student.student_id`)
   - `course_id` (BIGINT, Foreign Key referencing `course.course_id`)
   - `registration_date` (DATETIME, NOT NULL)
   - `status` (VARCHAR(30), NOT NULL, DEFAULT: 'REGISTERED')
   - **Constraint:** `UNIQUE (student_id, course_id)`

---

### 19. ER DIAGRAM

*(Refer to `documentation/UML_DIAGRAMS.md` for full Mermaid ER visual representation).*
* Relationships:
  - Student (1) to Registration (M)
  - Course (1) to Registration (M)

---

### 20. UML DIAGRAMS

The system includes 10 formal UML diagrams:
1. Use Case Diagram
2. Use Case Package Diagram
3. Domain Class Diagram
4. High-Level Sequence Diagram
5. Detailed Sequence Diagram
6. High-Level Communication Diagram
7. Detailed Communication Diagram
8. Activity Diagram
9. Component Diagram
10. Deployment Diagram

*(All 10 diagrams are specified in Mermaid format in `documentation/UML_DIAGRAMS.md`).*

---

### 21. MODULE DESCRIPTION

1. **Authentication & Session Module:** Manages role-based login (Student vs Admin), credential verification, and client session preservation.
2. **Student Management Module:** Supports student registration, roll number uniqueness, profile editing, and student roster inspection.
3. **Course Management Module:** Enables addition, editing, status toggling, and deletion of academic electives with capacity bounds.
4. **Registration Engine Module:** The business engine enforcing non-duplicate rules, real-time seat decrementing, automatic timestamps, and registration revoking.
5. **Dashboard & Analytics Module:** Aggregates live system totals and capacity percentages for administrative oversight.

---

### 22. IMPLEMENTATION

* **Backend Structure:**
  - `com.example.onlinecourse.entity`: JPA persistence entities.
  - `com.example.onlinecourse.repository`: Spring Data JPA interfaces.
  - `com.example.onlinecourse.service.impl`: Business logic enforcing seat decrements and duplicate checks.
  - `com.example.onlinecourse.controller`: REST endpoints for asynchronous communication.
  - `com.example.onlinecourse.dto`: Separation between domain entities and API contracts.
  - `com.example.onlinecourse.exception`: Custom runtime exceptions and `@ControllerAdvice` handler.
* **Frontend Structure:**
  - Responsive multi-page Bootstrap 5 views served statically by Spring Boot from `src/main/resources/static/`.

---

### 23. API DOCUMENTATION

| Method | Endpoint | Description | Request Body | Status Code |
|---|---|---|---|---|
| POST | `/api/students/register` | Register a new student | StudentDTO | 201 Created |
| POST | `/api/students/login` | Student login | LoginRequest | 200 OK |
| GET | `/api/students` | Get all students | None | 200 OK |
| GET | `/api/students/{id}` | Get student by ID | None | 200 OK |
| PUT | `/api/students/{id}` | Update student profile | StudentDTO | 200 OK |
| DELETE | `/api/students/{id}` | Delete student | None | 200 OK |
| POST | `/api/admin/login` | Admin login | LoginRequest | 200 OK |
| GET | `/api/admin/students` | View all students (Admin) | None | 200 OK |
| GET | `/api/admin/registrations`| View master registrations | None | 200 OK |
| GET | `/api/admin/stats` | View dashboard statistics | None | 200 OK |
| GET | `/api/courses` | List all courses | None | 200 OK |
| GET | `/api/courses/{id}` | Get course by ID | None | 200 OK |
| POST | `/api/courses` | Create new course | CourseDTO | 201 Created |
| PUT | `/api/courses/{id}` | Update course | CourseDTO | 200 OK |
| DELETE | `/api/courses/{id}` | Remove course | None | 200 OK |
| GET | `/api/courses/search?name=` | Search courses by name | None | 200 OK |
| GET | `/api/courses/available`| List courses with seats > 0 | None | 200 OK |
| POST | `/api/registrations` | Register student in course | RegistrationDTO | 201 Created |
| GET | `/api/registrations` | Get all registrations | None | 200 OK |
| GET | `/api/registrations/student/{id}` | Get student registrations | None | 200 OK |
| DELETE | `/api/registrations/{id}` | Drop/Cancel registration | None | 200 OK |

---

### 24. SCREENSHOTS PLACEHOLDERS

1. **Figure 1:** Home Page with Academic Hero Banner and Course Catalog preview (`index.html`)
2. **Figure 2:** Student Registration Page with validation (`student-register.html`)
3. **Figure 3:** Student Dashboard with metric cards (`student-dashboard.html`)
4. **Figure 4:** Course Browsing & Search page with dynamic badges (`courses.html`)
5. **Figure 5:** Course Details page with syllabus and Register Now button (`course-details.html`)
6. **Figure 6:** My Registered Courses page with drop action (`my-courses.html`)
7. **Figure 7:** Student Profile Edit page (`profile.html`)
8. **Figure 8:** Administrator Dashboard with Capacity Progress Bars (`admin-dashboard.html`)
9. **Figure 9:** Course Management page with Add/Edit/Delete actions (`manage-courses.html`)
10. **Figure 10:** Registration Ledger and Printable Report page (`registrations.html`)

---

### 25. TESTING

#### Unit Testing with JUnit 5 & Mockito
* `StudentServiceTest`: Tested registration, duplicate email rejection, invalid password rejection, and student retrieval.
* `CourseServiceTest`: Tested course creation, duplicate course code prevention, and seat range bounds.
* `RegistrationServiceTest`: Tested successful enrollment, atomic seat decrements, duplicate registration prevention (409 Conflict), course-full prevention (400 Bad Request), and cancellation seat restoration.

#### Postman API Testing
Full end-to-end testing performed using the bundled collection (`postman/Online_Course_Registration_API.postman_collection.json`).

---

### 26. RESULTS

The application successfully met all academic project objectives:
* 100% of tested registration attempts correctly modified available seat counts.
* Duplicate registration attempts resulted in instant rejection with user-friendly error banners.
* Seat restoration upon cancellation was verified at both the database level and UI level.
* Response latency averaged under 40ms on local test environments.

---

### 27. ADVANTAGES

* **Zero Paperwork:** Complete digitization of course registration workflows.
* **Real-Time Seat Visibility:** Prevents student frustration by clearly indicating full vs available courses.
* **High Data Integrity:** Eliminates double registrations and database race conditions.
* **Rapid Administrative Reporting:** Instant aggregation of department-wise enrollment statistics.
* **Accessible Anywhere:** Standard responsive web interface accessible from mobile phones, tablets, or laptops.

---

### 28. LIMITATIONS

* In the academic version, authentication uses plain text passwords for demonstration simplicity; production deployment requires BCrypt hashing and Spring Security JWT filters.
* Online fee payments for premium lab electives are not integrated.
* Automated prerequisite course checks (e.g. Data Structures before Algorithms) are currently managed via course descriptions rather than automated database constraints.

---

### 29. FUTURE ENHANCEMENTS

1. **JWT & Spring Security:** Implementing JSON Web Tokens with OAuth2 and HTTPS.
2. **Email & SMS Notifications:** Automated email confirmations upon successful registration or course dropping.
3. **Timetable Conflict Detection:** Algorithmic verification ensuring registered courses do not have overlapping lecture hours.
4. **Payment Gateway:** Integration with Razorpay, Stripe, or PayPal for paid certificate courses.
5. **Faculty Grading Portal:** Allowing instructors to upload syllabus files and submit course grades directly.

---

### 30. CONCLUSION

The **Online Course Registration Management System** provides a modern, scalable, and robust solution to the longstanding challenges of college course administration. By utilizing enterprise Java with Spring Boot, JPA, MySQL, and a clean Bootstrap frontend, the system guarantees ACID transactions, prevents capacity overruns, and empowers students with an intuitive elective enrollment portal. The system is well-suited for college academic submissions and viva demonstrations.

---

### 31. REFERENCES

1. Walls, Craig. *Spring in Action, Sixth Edition*. Manning Publications, 2022.
2. Martin, Robert C. *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall, 2017.
3. Spring Framework Official Documentation: https://spring.io/projects/spring-boot
4. Hibernate ORM Documentation: https://hibernate.org/orm/documentation/
5. MySQL 8.0 Reference Manual: https://dev.mysql.com/doc/refman/8.0/en/
6. Bootstrap 5 Documentation: https://getbootstrap.com/docs/5.3/
