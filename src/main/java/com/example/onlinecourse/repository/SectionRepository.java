package com.example.onlinecourse.repository;

import com.example.onlinecourse.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByMinorStreamId(Long minorStreamId);

    List<Section> findByMinorStreamIdAndActiveTrue(Long minorStreamId);

    List<Section> findByBranchId(Long branchId);

    List<Section> findByFacultyId(Long facultyId);

    long countByMinorStreamId(Long minorStreamId);

    List<Section> findByActiveTrue();
}
