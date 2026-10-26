package com.example.onlinecourse.repository;

import com.example.onlinecourse.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByBranchCodeIgnoreCase(String branchCode);

    boolean existsByBranchCodeIgnoreCase(String branchCode);

    List<Branch> findByActiveTrue();
}
