package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.DashboardStatsDTO;
import com.example.onlinecourse.dto.LoginRequest;
import com.example.onlinecourse.dto.LoginResponse;
import com.example.onlinecourse.entity.Admin;

import java.util.List;

/**
 * Service interface for admin-related operations including authentication
 * and dashboard statistics retrieval.
 */
public interface AdminService {

    /**
     * Authenticates an admin user using their email and password.
     *
     * @param loginRequest the login credentials
     * @return a {@link LoginResponse} containing authentication result and token
     */
    LoginResponse loginAdmin(LoginRequest loginRequest);

    /**
     * Authenticates a faculty user using their email and password.
     *
     * @param loginRequest the login credentials
     * @return a {@link LoginResponse} containing authentication result and token
     */
    LoginResponse loginFaculty(LoginRequest loginRequest);

    /**
     * Retrieves an admin by their unique ID.
     *
     * @param id the admin's ID
     * @return the {@link Admin} entity
     */
    Admin getAdminById(Long id);

    /**
     * Retrieves all admins in the system.
     *
     * @return a list of {@link Admin} entities
     */
    List<Admin> getAllAdmins();

    /**
     * Retrieves aggregated dashboard statistics for the admin panel.
     *
     * @return a {@link DashboardStatsDTO} with counts of key entities
     */
    DashboardStatsDTO getDashboardStats();
}
