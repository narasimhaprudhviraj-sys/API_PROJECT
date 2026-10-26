package com.example.onlinecourse.repository;

import com.example.onlinecourse.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    List<Course> findByCourseNameContainingIgnoreCase(String courseName);

    List<Course> findByDepartmentIgnoreCase(String department);

    List<Course> findByAvailableSeatsGreaterThan(int seats);

    List<Course> findByStatusIgnoreCase(String status);

    List<Course> findByFacultyId(Long facultyId);

    List<Course> findByMinorStreamId(Long minorStreamId);

    List<Course> findBySemesterId(Long semesterId);

    /**
     * CORE REQUIREMENT: Find courses strictly matching student's branch, fixed minor stream, and semester.
     */
    @Query("SELECT c FROM Course c WHERE c.branch.id = :branchId " +
           "AND c.minorStream.id = :minorStreamId " +
           "AND c.semester.id = :semesterId " +
           "AND UPPER(c.status) = 'ACTIVE'")
    List<Course> findEligibleCourses(
            @Param("branchId") Long branchId,
            @Param("minorStreamId") Long minorStreamId,
            @Param("semesterId") Long semesterId
    );

    @Query("SELECT c FROM Course c WHERE " +
           "(:keyword IS NULL OR LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.facultyName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:department IS NULL OR LOWER(c.department) = LOWER(:department)) AND " +
           "(:onlyAvailable = false OR c.availableSeats > 0)")
    List<Course> filterCourses(
            @Param("keyword") String keyword,
            @Param("department") String department,
            @Param("onlyAvailable") boolean onlyAvailable
    );

    Page<Course> findAll(Pageable pageable);
}
