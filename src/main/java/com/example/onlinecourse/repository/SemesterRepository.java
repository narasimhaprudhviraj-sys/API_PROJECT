package com.example.onlinecourse.repository;

import com.example.onlinecourse.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    Optional<Semester> findBySemesterNumber(Integer semesterNumber);

    boolean existsBySemesterNumber(Integer semesterNumber);

    List<Semester> findByActiveTrueOrderBySemesterNumberAsc();
}
