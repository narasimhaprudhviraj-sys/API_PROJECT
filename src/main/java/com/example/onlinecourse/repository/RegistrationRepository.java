package com.example.onlinecourse.repository;

import com.example.onlinecourse.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByStudentStudentId(Long studentId);

    List<Registration> findByStudentStudentIdAndStatus(Long studentId, String status);

    List<Registration> findByCourseCourseId(Long courseId);

    boolean existsByStudentStudentIdAndCourseCourseId(Long studentId, Long courseId);

    boolean existsByStudentStudentIdAndCourseCourseIdAndStatus(Long studentId, Long courseId, String status);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Registration r " +
           "WHERE r.student.studentId = :studentId AND r.course.courseId = :courseId " +
           "AND (:semesterId IS NULL OR r.semester.id = :semesterId) " +
           "AND UPPER(r.status) = 'REGISTERED'")
    boolean isStudentRegisteredForCourseInSemester(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("semesterId") Long semesterId
    );

    Optional<Registration> findByStudentStudentIdAndCourseCourseId(Long studentId, Long courseId);

    Optional<Registration> findByStudentStudentIdAndCourseCourseIdAndSemesterId(Long studentId, Long courseId, Long semesterId);

    Optional<Registration> findByStudentStudentIdAndCourseCourseIdAndStatus(Long studentId, Long courseId, String status);

    long countByCourseCourseId(Long courseId);

    long countByCourseCourseIdAndStatus(Long courseId, String status);

    long countByStatus(String status);

    Page<Registration> findAll(Pageable pageable);
}
