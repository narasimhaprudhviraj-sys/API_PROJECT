package com.example.onlinecourse.repository;

import com.example.onlinecourse.entity.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByEmailIgnoreCase(String email);

    default Optional<Faculty> findByEmail(String email) {
        return findByEmailIgnoreCase(email);
    }

    Optional<Faculty> findByFacultyCodeIgnoreCase(String facultyCode);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByFacultyCodeIgnoreCase(String facultyCode);

    List<Faculty> findByDepartmentIgnoreCase(String department);

    List<Faculty> findByActiveTrue();

    Page<Faculty> findAll(Pageable pageable);
}
