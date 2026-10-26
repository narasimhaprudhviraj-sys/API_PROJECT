/* ===================================================================
   Online Course Registration Management System
   Centralized Frontend API & UI Logic with JWT Auth Support
   =================================================================== */

const API_BASE = 'http://localhost:8080/api';

// --- Toast / Alert Helper ---
function showAlert(message, type = 'success') {
    let container = document.getElementById('alertContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'alertContainer';
        container.style.position = 'fixed';
        container.style.top = '20px';
        container.style.right = '20px';
        container.style.zIndex = '9999';
        container.style.minWidth = '300px';
        document.body.appendChild(container);
    }

    const alertDiv = document.createElement('div');
    const alertType = type === 'error' ? 'danger' : type;
    alertDiv.className = `alert alert-${alertType} alert-dismissible fade show shadow-sm`;
    alertDiv.role = 'alert';
    alertDiv.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="bi ${alertType === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill'} me-2"></i>
            <div>${message}</div>
            <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;

    container.appendChild(alertDiv);

    setTimeout(() => {
        alertDiv.classList.remove('show');
        setTimeout(() => alertDiv.remove(), 300);
    }, 4500);
}

// --- Token Extraction Helper ---
function getAuthToken() {
    const directToken = localStorage.getItem('token');
    if (directToken) return directToken;

    const student = localStorage.getItem('student_user');
    if (student) {
        try {
            const parsed = JSON.parse(student);
            if (parsed.token) return parsed.token;
        } catch (e) {}
    }

    const admin = localStorage.getItem('admin_user');
    if (admin) {
        try {
            const parsed = JSON.parse(admin);
            if (parsed.token) return parsed.token;
        } catch (e) {}
    }

    const faculty = localStorage.getItem('faculty_user');
    if (faculty) {
        try {
            const parsed = JSON.parse(faculty);
            if (parsed.token) return parsed.token;
        } catch (e) {}
    }

    return null;
}

// --- Session Storage Management ---
const Auth = {
    setStudent(student) {
        localStorage.setItem('student_user', JSON.stringify(student));
        if (student && student.token) {
            localStorage.setItem('token', student.token);
            localStorage.setItem('role', 'STUDENT');
            localStorage.setItem('userId', student.userId);
            localStorage.setItem('name', student.name);
        }
    },
    getStudent() {
        const data = localStorage.getItem('student_user');
        return data ? JSON.parse(data) : null;
    },
    clearStudent() {
        localStorage.removeItem('student_user');
    },

    setAdmin(admin) {
        localStorage.setItem('admin_user', JSON.stringify(admin));
        if (admin && admin.token) {
            localStorage.setItem('token', admin.token);
            localStorage.setItem('role', 'ADMIN');
            localStorage.setItem('userId', admin.userId);
            localStorage.setItem('name', admin.name);
        }
    },
    getAdmin() {
        const data = localStorage.getItem('admin_user');
        return data ? JSON.parse(data) : null;
    },
    clearAdmin() {
        localStorage.removeItem('admin_user');
    },

    setFaculty(faculty) {
        localStorage.setItem('faculty_user', JSON.stringify(faculty));
        if (faculty && faculty.token) {
            localStorage.setItem('token', faculty.token);
            localStorage.setItem('role', 'FACULTY');
            localStorage.setItem('userId', faculty.userId);
            localStorage.setItem('name', faculty.name);
        }
    },
    getFaculty() {
        const data = localStorage.getItem('faculty_user');
        return data ? JSON.parse(data) : null;
    },
    clearFaculty() {
        localStorage.removeItem('faculty_user');
    },

    logout() {
        Auth.clearStudent();
        Auth.clearAdmin();
        Auth.clearFaculty();
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        localStorage.removeItem('userId');
        localStorage.removeItem('name');
        window.location.href = 'index.html';
    }
};

// --- Page Access Protectors ---
function requireStudentAuth() {
    const student = Auth.getStudent();
    if (!student || !student.userId) {
        showAlert('Please login as a student to access this page.', 'warning');
        setTimeout(() => {
            window.location.href = 'student-login.html';
        }, 1000);
        return null;
    }
    return student;
}

function requireAdminAuth() {
    const admin = Auth.getAdmin();
    if (!admin || !admin.userId) {
        showAlert('Please login as an administrator to access this page.', 'warning');
        setTimeout(() => {
            window.location.href = 'admin-login.html';
        }, 1000);
        return null;
    }
    return admin;
}

function requireFacultyAuth() {
    const faculty = Auth.getFaculty();
    if (!faculty || !faculty.userId) {
        showAlert('Please login as faculty to access this page.', 'warning');
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 1000);
        return null;
    }
    return faculty;
}

// --- Generic Fetch Wrapper with JWT Header & Error Handling ---
async function apiCall(endpoint, method = 'GET', body = null) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    };

    const token = getAuthToken();
    if (token) {
        options.headers['Authorization'] = `Bearer ${token}`;
    }

    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, options);

        if (response.status === 401) {
            showAlert('Session expired or unauthorized. Please log in again.', 'warning');
            setTimeout(() => {
                Auth.logout();
            }, 1200);
            throw new Error('Unauthorized');
        }

        if (response.status === 403) {
            showAlert('Access Denied: You do not have permission to perform this action.', 'danger');
            throw new Error('Access Denied');
        }

        if (response.status === 204) {
            return null;
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            let errorMsg = 'An error occurred. Please try again.';
            if (data) {
                if (data.message) {
                    errorMsg = data.message;
                } else if (data.validationErrors && data.validationErrors.length > 0) {
                    errorMsg = data.validationErrors.join(', ');
                } else if (data.error) {
                    errorMsg = data.error;
                }
            }
            throw new Error(errorMsg);
        }

        return data;
    } catch (err) {
        throw err;
    }
}

// --- Formatters ---
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        return dateString;
    }
}

function getSeatBadge(available, max) {
    if (available <= 0) {
        return `<span class="badge bg-danger">FULL (0/${max})</span>`;
    } else if (available <= 5) {
        return `<span class="badge bg-warning text-dark">Few Left (${available}/${max})</span>`;
    } else {
        return `<span class="badge bg-success">${available}/${max} Seats Available</span>`;
    }
}

// --- Global Navbar Loader / Header Renderer ---
document.addEventListener('DOMContentLoaded', () => {
    const navUserContainer = document.getElementById('navUserSection');
    if (navUserContainer) {
        const student = Auth.getStudent();
        const admin = Auth.getAdmin();
        const faculty = Auth.getFaculty();

        if (student) {
            navUserContainer.innerHTML = `
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle text-white fw-bold" href="#" id="studentDropdown" role="button" data-bs-toggle="dropdown">
                        <i class="bi bi-person-circle me-1"></i> ${student.name}
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end shadow">
                        <li><a class="dropdown-item" href="student-dashboard.html"><i class="bi bi-speedometer2 me-2"></i>Dashboard</a></li>
                        <li><a class="dropdown-item" href="courses.html"><i class="bi bi-book me-2"></i>Browse Courses</a></li>
                        <li><a class="dropdown-item" href="my-courses.html"><i class="bi bi-journal-check me-2"></i>My Registered Courses</a></li>
                        <li><a class="dropdown-item" href="profile.html"><i class="bi bi-person me-2"></i>My Profile</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="Auth.logout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
                    </ul>
                </li>
            `;
        } else if (admin) {
            navUserContainer.innerHTML = `
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle text-white fw-bold" href="#" id="adminDropdown" role="button" data-bs-toggle="dropdown">
                        <i class="bi bi-shield-lock-fill me-1"></i> Admin: ${admin.name}
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end shadow">
                        <li><a class="dropdown-item" href="admin-dashboard.html"><i class="bi bi-speedometer2 me-2"></i>Dashboard</a></li>
                        <li><a class="dropdown-item" href="manage-courses.html"><i class="bi bi-journal-bookmark me-2"></i>Manage Courses</a></li>
                        <li><a class="dropdown-item" href="add-course.html"><i class="bi bi-plus-circle me-2"></i>Add Course</a></li>
                        <li><a class="dropdown-item" href="manage-students.html"><i class="bi bi-people me-2"></i>Manage Students</a></li>
                        <li><a class="dropdown-item" href="registrations.html"><i class="bi bi-card-checklist me-2"></i>All Registrations</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="Auth.logout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
                    </ul>
                </li>
            `;
        } else if (faculty) {
            navUserContainer.innerHTML = `
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle text-white fw-bold" href="#" id="facultyDropdown" role="button" data-bs-toggle="dropdown">
                        <i class="bi bi-mortarboard-fill me-1"></i> Faculty: ${faculty.name}
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end shadow">
                        <li><a class="dropdown-item" href="faculty-dashboard.html"><i class="bi bi-speedometer2 me-2"></i>Dashboard</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="Auth.logout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
                    </ul>
                </li>
            `;
        } else {
            navUserContainer.innerHTML = `
                <li class="nav-item"><a class="nav-link" href="student-login.html"><i class="bi bi-person-fill"></i> Student Login</a></li>
                <li class="nav-item"><a class="nav-link" href="student-register.html"><i class="bi bi-person-plus-fill"></i> Register</a></li>
                <li class="nav-item"><a class="btn btn-warning btn-sm ms-2 text-dark fw-bold px-3" href="admin-login.html"><i class="bi bi-shield-lock"></i> Admin Portal</a></li>
            `;
        }
    }
});
