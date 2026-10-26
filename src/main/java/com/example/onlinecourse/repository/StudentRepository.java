package com.example.onlinecourse.repository;

import com.example.onlinecourse.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    Optional<Student> findByRollNo(String rollNo);

    boolean existsByEmail(String email);

    boolean existsByRollNo(String rollNo);

    List<Student> findBySectionId(Long sectionId);

    List<Student> findByBranchId(Long branchId);

    List<Student> findByMinorStreamId(Long minorStreamId);

    /**
     * Requirement: Faculty can ONLY see students in their assigned sections!
     */
    @Query("SELECT s FROM Student s WHERE s.section.faculty.id = :facultyId")
    List<Student> findStudentsByAssignedFacultyId(@Param("facultyId") Long facultyId);

    Page<Student> findAll(Pageable pageable);

    Page<Student> findByNameContainingIgnoreCaseOrRollNoContainingIgnoreCase(
            String name, String rollNo, Pageable pageable
    );
}
