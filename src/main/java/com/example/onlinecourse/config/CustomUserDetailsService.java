package com.example.onlinecourse.config;

import com.example.onlinecourse.entity.Admin;
import com.example.onlinecourse.entity.Faculty;
import com.example.onlinecourse.entity.Student;
import com.example.onlinecourse.repository.AdminRepository;
import com.example.onlinecourse.repository.FacultyRepository;
import com.example.onlinecourse.repository.StudentRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Loads UserDetails from the database across Admin, Faculty, and Student entities.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    public CustomUserDetailsService(AdminRepository adminRepository,
                                    FacultyRepository facultyRepository,
                                    StudentRepository studentRepository) {
        this.adminRepository = adminRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String cleanEmail = email.trim().toLowerCase();

        // 1. Check Admin
        Optional<Admin> adminOpt = adminRepository.findByEmail(cleanEmail);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            return new UserPrincipal(admin.getAdminId(), admin.getEmail(), admin.getPassword(), "ROLE_ADMIN", admin.getName());
        }

        // 2. Check Faculty
        Optional<Faculty> facultyOpt = facultyRepository.findByEmailIgnoreCase(cleanEmail);
        if (facultyOpt.isPresent()) {
            Faculty faculty = facultyOpt.get();
            return new UserPrincipal(faculty.getId(), faculty.getEmail(), faculty.getPassword(), "ROLE_FACULTY", faculty.getName());
        }

        // 3. Check Student
        Optional<Student> studentOpt = studentRepository.findByEmail(cleanEmail);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            return new UserPrincipal(student.getStudentId(), student.getEmail(), student.getPassword(), "ROLE_STUDENT", student.getName());
        }

        throw new UsernameNotFoundException("User account not found with email: " + email);
    }
}
