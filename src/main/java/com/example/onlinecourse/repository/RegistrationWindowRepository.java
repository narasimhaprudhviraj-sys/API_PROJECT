package com.example.onlinecourse.repository;

import com.example.onlinecourse.entity.RegistrationWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationWindowRepository extends JpaRepository<RegistrationWindow, Long> {

    List<RegistrationWindow> findBySemesterId(Long semesterId);

    List<RegistrationWindow> findByActiveTrue();

    @Query("SELECT rw FROM RegistrationWindow rw WHERE rw.semester.id = :semesterId " +
           "AND rw.active = true AND UPPER(rw.status) = 'OPEN' " +
           "AND :now BETWEEN rw.startDateTime AND rw.endDateTime")
    Optional<RegistrationWindow> findOpenWindowForSemester(@Param("semesterId") Long semesterId,
                                                          @Param("now") LocalDateTime now);

    @Query("SELECT rw FROM RegistrationWindow rw WHERE rw.active = true " +
           "AND UPPER(rw.status) = 'OPEN' AND :now BETWEEN rw.startDateTime AND rw.endDateTime")
    List<RegistrationWindow> findAllCurrentlyOpenWindows(@Param("now") LocalDateTime now);
}
