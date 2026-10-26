# College Viva Questions and Answers
## Online Course Registration Management System

This document prepares students for external project viva, technical interviews, and university laboratory examinations.

---

### Q1: What is the main objective of the Online Course Registration Management System?
**Answer:**  
The main objective is to automate and streamline the college elective registration process. It provides a web-based self-service platform where students can browse available courses, check real-time seat capacities, and enroll in courses without duplicate registrations or over-enrollment, while allowing administrators to maintain courses, inspect student rosters, and track seat capacity metrics.

---

### Q2: What is the architecture of your project?
**Answer:**  
The project uses a clean **3-tier Layered Architecture**:
1. **Presentation Layer:** HTML5, CSS3, Bootstrap 5, and JavaScript communicating asynchronously via the `fetch` API.
2. **Business / Controller Layer:** Spring Boot REST Controllers (`@RestController`), Service Interfaces, and Service Implementations (`@Service`, `@Transactional`).
3. **Data Persistence Layer:** Spring Data JPA with Hibernate ORM communicating with a **MySQL 8.0** relational database.

---

### Q3: How do you prevent duplicate course registrations for the same student?
**Answer:**  
We prevent duplicate registrations at two distinct levels:
1. **Database Level:** We enforce a unique composite constraint on the `registration` table:  
   `UNIQUE (student_id, course_id)`.  
   If an identical record is inserted, the database throws a constraint violation exception.
2. **Service Layer Level:** Before executing the save operation, `RegistrationServiceImpl` calls:  
   `registrationRepository.findByStudentStudentIdAndCourseCourseId(studentId, courseId)`.  
   If an active registration exists, the service throws a custom `DuplicateRegistrationException`, which `GlobalExceptionHandler` converts into an HTTP `409 Conflict` status with an informative error message.

---

### Q4: How is real-time seat capacity handled when a student registers or cancels a course?
**Answer:**  
In `RegistrationServiceImpl`:
* **During Registration:**  
  1. The system checks `course.getAvailableSeats() > 0`. If 0, it throws `CourseFullException` (`400 Bad Request`).
  2. If seats are available, `availableSeats` is decremented by 1:  
     `course.setAvailableSeats(course.getAvailableSeats() - 1);`  
     and saved to MySQL.
* **During Cancellation (Course Drop):**  
  1. The system verifies that the registration status was active.
  2. If `availableSeats < maxSeats`, it increments `availableSeats` by 1:  
     `course.setAvailableSeats(course.getAvailableSeats() + 1);`
  3. The registration record is removed or marked `CANCELLED`.  
All of this occurs inside a `@Transactional` block to guarantee ACID consistency.

---

### Q5: What is the role of Spring Data JPA in this project?
**Answer:**  
Spring Data JPA significantly reduces boilerplate data-access code. By extending `JpaRepository<Entity, ID>`, our repositories (`StudentRepository`, `CourseRepository`, `RegistrationRepository`, `AdminRepository`) automatically inherit standard CRUD operations (`save`, `findById`, `findAll`, `deleteById`) and query methods based on method naming conventions like `findByEmail` or `existsByCourseCode`. It also allows custom JPQL queries using `@Query`.

---

### Q6: Why did you use DTOs (Data Transfer Objects) instead of returning Entities directly to the UI?
**Answer:**  
We use DTOs for four primary reasons:
1. **Security:** Entities like `Student` contain sensitive fields like `password`. DTOs like `StudentResponseDTO` omit the password so it is never exposed in API responses.
2. **Decoupling:** Decouples internal database schema modifications from the public REST API contracts.
3. **Circular Reference Prevention:** Bi-directional JPA relationships (such as `@OneToMany` between `Student` and `Registration`) cause infinite JSON recursion if entities are serialized directly. DTOs flatten the data structure cleanly.
4. **Validation:** Request DTOs carry validation annotations (`@NotBlank`, `@Email`, `@Min`, `@Pattern`) to ensure clean input.

---

### Q7: How is exception handling handled globally in Spring Boot?
**Answer:**  
We use `@ControllerAdvice` in `GlobalExceptionHandler.java`. It centralizes error handling across all controllers using `@ExceptionHandler` methods. It captures:
* `ResourceNotFoundException` -> 404 NOT FOUND
* `DuplicateRegistrationException` -> 409 CONFLICT
* `CourseFullException` -> 400 BAD REQUEST
* `MethodArgumentNotValidException` -> 400 BAD REQUEST with validation error lists
* `InvalidCredentialsException` -> 401 UNAUTHORIZED  
Each handler returns a standardized `ErrorDetails` JSON object containing timestamp, HTTP status, error name, custom message, and request URI.

---

### Q8: What HTTP methods did you use and what are their purposes?
**Answer:**  
* **POST:** Used to create resources (e.g. register student, login, add course, enroll in course).
* **GET:** Used to retrieve resources (e.g. fetch all courses, search courses, get student by ID, view dashboard metrics).
* **PUT:** Used to update existing resources (e.g. update student profile, edit course details).
* **DELETE:** Used to remove resources or cancel registrations (e.g. cancel registration, delete course).

---

### Q9: What is CORS and how did you configure it?
**Answer:**  
CORS (Cross-Origin Resource Sharing) is a browser security mechanism that blocks web pages from making AJAX requests to a different domain or port than the one that served the page. Since our frontend might run on a Live Server (e.g. port 5500) or directly from file protocols while Spring Boot runs on port 8080, we configured `CorsConfig.java` implementing `WebMvcConfigurer` to allow `GET, POST, PUT, DELETE, OPTIONS` from any origin (`*`).

---

### Q10: What happens if two students click "Register" on the last remaining seat at the same second?
**Answer:**  
The method is decorated with Spring's `@Transactional` annotation. In MySQL InnoDB, row-level locking occurs during the UPDATE on the course record. The first transaction to acquire the lock decrements the seat count to 0 and commits. The second transaction attempts to evaluate `availableSeats > 0`, finds that `availableSeats == 0`, and throws `CourseFullException`, rolling back cleanly and preventing negative seat counts.

---

### Q11: How is the database automatically seeded on application startup?
**Answer:**  
We created a Spring Component called `DataInitializer` that implements `CommandLineRunner`. During boot, its `run()` method executes. If the repositories are empty, it automatically populates the default Administrator (`admin@college.edu`), 8 diverse academic courses, 5 realistic sample students, and initial course enrollments.

---

### Q12: Can this application be deployed on the cloud?
**Answer:**  
Yes. Spring Boot compiles into a standalone executable `.jar` file with an embedded Tomcat server. It can be containerized using a Dockerfile and deployed to platforms like AWS Elastic Beanstalk, Google Cloud Run, Microsoft Azure App Service, or Heroku, with MySQL hosted on AWS RDS or Clever Cloud.
