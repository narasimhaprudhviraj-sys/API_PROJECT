package com.example.onlinecourse.repository;

import com.example.onlinecourse.entity.MinorStream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MinorStreamRepository extends JpaRepository<MinorStream, Long> {

    List<MinorStream> findByBranchId(Long branchId);

    List<MinorStream> findByBranchIdAndActiveTrue(Long branchId);

    Optional<MinorStream> findByStreamCodeIgnoreCase(String streamCode);

    boolean existsByStreamCodeIgnoreCase(String streamCode);

    long countByBranchId(Long branchId);

    List<MinorStream> findByActiveTrue();
}
