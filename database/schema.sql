-- =====================================================================
-- COMPLETE DATABASE SCHEMA SCRIPT
-- ONLINE COURSE REGISTRATION MANAGEMENT SYSTEM
-- Target DBMS: MySQL 8.0+
-- =====================================================================

CREATE DATABASE IF NOT EXISTS online_course_registration
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE online_course_registration;

-- -----------------------------------------------------
-- Drop tables in reverse foreign-key dependency order
-- -----------------------------------------------------
DROP TABLE IF EXISTS registration;
DROP TABLE IF EXISTS registration_window;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS section;
DROP TABLE IF EXISTS minor_stream;
DROP TABLE IF EXISTS semester;
DROP TABLE IF EXISTS faculty;
DROP TABLE IF EXISTS branch;
DROP TABLE IF EXISTS admin;

-- -----------------------------------------------------
-- 1. Table: admin
-- -----------------------------------------------------
CREATE TABLE admin (
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 2. Table: branch
-- -----------------------------------------------------
CREATE TABLE branch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_code VARCHAR(20) NOT NULL UNIQUE,
    branch_name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 3. Table: minor_stream
-- -----------------------------------------------------
CREATE TABLE minor_stream (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stream_code VARCHAR(20) NOT NULL UNIQUE,
    stream_name VARCHAR(100) NOT NULL,
    branch_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_minor_stream_branch FOREIGN KEY (branch_id) REFERENCES branch (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 4. Table: faculty
-- -----------------------------------------------------
CREATE TABLE faculty (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    department VARCHAR(50) NOT NULL,
    designation VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 5. Table: semester
-- -----------------------------------------------------
CREATE TABLE semester (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    semester_number INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_semester_year UNIQUE (semester_number, academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 6. Table: section
-- -----------------------------------------------------
CREATE TABLE section (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_name VARCHAR(50) NOT NULL,
    branch_id BIGINT NOT NULL,
    minor_stream_id BIGINT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    faculty_id BIGINT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_section_branch FOREIGN KEY (branch_id) REFERENCES branch (id) ON DELETE CASCADE,
    CONSTRAINT fk_section_minor_stream FOREIGN KEY (minor_stream_id) REFERENCES minor_stream (id) ON DELETE CASCADE,
    CONSTRAINT fk_section_faculty FOREIGN KEY (faculty_id) REFERENCES faculty (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 7. Table: course
-- Includes version column for JPA optimistic locking
-- -----------------------------------------------------
CREATE TABLE course (
    course_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(50) NOT NULL UNIQUE,
    course_name VARCHAR(150) NOT NULL,
    description TEXT,
    department VARCHAR(50) NOT NULL,
    credits INT NOT NULL,
    faculty_name VARCHAR(100),
    max_seats INT NOT NULL,
    available_seats INT NOT NULL,
    duration VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    branch_id BIGINT NULL,
    minor_stream_id BIGINT NULL,
    semester_id BIGINT NULL,
    faculty_id BIGINT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_course_seats CHECK (available_seats >= 0 AND available_seats <= max_seats),
    CONSTRAINT chk_course_credits CHECK (credits > 0),
    CONSTRAINT fk_course_branch FOREIGN KEY (branch_id) REFERENCES branch (id) ON DELETE SET NULL,
    CONSTRAINT fk_course_minor_stream FOREIGN KEY (minor_stream_id) REFERENCES minor_stream (id) ON DELETE SET NULL,
    CONSTRAINT fk_course_semester FOREIGN KEY (semester_id) REFERENCES semester (id) ON DELETE SET NULL,
    CONSTRAINT fk_course_faculty FOREIGN KEY (faculty_id) REFERENCES faculty (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 8. Table: student
-- Fixed minor stream & branch assigned from 2nd year
-- -----------------------------------------------------
CREATE TABLE student (
    student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roll_no VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL,
    academic_year INT NOT NULL DEFAULT 2,
    phone VARCHAR(20) NOT NULL,
    branch_id BIGINT NULL,
    minor_stream_id BIGINT NULL,
    section_id BIGINT NULL,
    semester_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_student_year CHECK (academic_year BETWEEN 1 AND 4),
    CONSTRAINT fk_student_branch FOREIGN KEY (branch_id) REFERENCES branch (id) ON DELETE SET NULL,
    CONSTRAINT fk_student_minor_stream FOREIGN KEY (minor_stream_id) REFERENCES minor_stream (id) ON DELETE SET NULL,
    CONSTRAINT fk_student_section FOREIGN KEY (section_id) REFERENCES section (id) ON DELETE SET NULL,
    CONSTRAINT fk_student_semester FOREIGN KEY (semester_id) REFERENCES semester (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 9. Table: registration_window
-- -----------------------------------------------------
CREATE TABLE registration_window (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    semester_id BIGINT NOT NULL,
    start_date_time DATETIME NOT NULL,
    end_date_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_reg_window_semester FOREIGN KEY (semester_id) REFERENCES semester (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 10. Table: registration
-- Status: REGISTERED, DROPPED, CANCELLED
-- Unique constraint prevents concurrent duplicate registrations
-- -----------------------------------------------------
CREATE TABLE registration (
    registration_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    semester_id BIGINT NULL,
    registration_date DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',
    CONSTRAINT fk_reg_student FOREIGN KEY (student_id) REFERENCES student (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_course FOREIGN KEY (course_id) REFERENCES course (course_id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_semester FOREIGN KEY (semester_id) REFERENCES semester (id) ON DELETE SET NULL,
    CONSTRAINT uq_student_course_semester UNIQUE (student_id, course_id, semester_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Performance Indices
CREATE INDEX idx_branch_code ON branch (branch_code);
CREATE INDEX idx_faculty_code ON faculty (faculty_code);
CREATE INDEX idx_minor_stream_branch ON minor_stream (branch_id);
CREATE INDEX idx_section_stream ON section (minor_stream_id);
CREATE INDEX idx_course_eligibility ON course (branch_id, minor_stream_id, semester_id, status);
CREATE INDEX idx_reg_student ON registration (student_id);
CREATE INDEX idx_reg_course ON registration (course_id);
CREATE INDEX idx_window_semester ON registration_window (semester_id, active, status);
