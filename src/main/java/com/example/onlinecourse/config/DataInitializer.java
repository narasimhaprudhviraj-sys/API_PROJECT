package com.example.onlinecourse.config;

import com.example.onlinecourse.entity.*;
import com.example.onlinecourse.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataInitializer seeds the database with initial data for the college
 * online course registration system. Each entity group is only seeded
 * if its corresponding table is empty (count() == 0).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final BranchRepository branchRepository;
    private final MinorStreamRepository minorStreamRepository;
    private final FacultyRepository facultyRepository;
    private final SectionRepository sectionRepository;
    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;
    private final RegistrationWindowRepository registrationWindowRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.initial-seed.enabled:true}")
    private boolean seedEnabled;

    /**
     * Constructor injection of all required repositories and the password encoder.
     */
    public DataInitializer(AdminRepository adminRepository,
                           BranchRepository branchRepository,
                           MinorStreamRepository minorStreamRepository,
                           FacultyRepository facultyRepository,
                           SectionRepository sectionRepository,
                           SemesterRepository semesterRepository,
                           CourseRepository courseRepository,
                           StudentRepository studentRepository,
                           RegistrationRepository registrationRepository,
                           RegistrationWindowRepository registrationWindowRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.branchRepository = branchRepository;
        this.minorStreamRepository = minorStreamRepository;
        this.facultyRepository = facultyRepository;
        this.sectionRepository = sectionRepository;
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
        this.registrationWindowRepository = registrationWindowRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Entry point called by Spring Boot on startup.
     * Seeds all entity groups in dependency order if seeding is enabled.
     */
    @Override
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            System.out.println("[DataInitializer] Seeding is disabled via configuration.");
            return;
        }

        System.out.println("[DataInitializer] Starting database seed...");

        seedAdmin();

        Map<String, Branch> branches = seedBranches();
        Map<String, MinorStream> minorStreams = seedMinorStreams(branches);
        Map<String, Faculty> faculty = seedFaculty();
        Map<Integer, Semester> semesters = seedSemesters();
        Map<String, Section> sections = seedSections(branches, minorStreams, faculty);

        seedCourses(branches, minorStreams, faculty, semesters);
        seedStudents(branches, minorStreams, sections, semesters);
        seedRegistrationWindows(semesters);

        System.out.println("[DataInitializer] Database seeding complete.");
    }

    // -------------------------------------------------------------------------
    // Admin
    // -------------------------------------------------------------------------

    /**
     * Seeds the default system administrator account if none exists.
     */
    private void seedAdmin() {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin("System Administrator", "admin@college.edu", passwordEncoder.encode("admin123"));
            adminRepository.save(admin);
            System.out.println("[DataInitializer] Seeded Admin: System Administrator");
        } else {
            adminRepository.findByEmail("admin@college.edu").ifPresent(admin -> {
                if (!admin.getPassword().startsWith("$2a$") && !admin.getPassword().startsWith("$2b$")) {
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    adminRepository.save(admin);
                    System.out.println("[DataInitializer] Upgraded Admin password to BCrypt hash.");
                }
            });
        }
    }

    // -------------------------------------------------------------------------
    // Branches
    // -------------------------------------------------------------------------

    /**
     * Seeds the three degree branches and returns a map keyed by branch code.
     *
     * @return Map of branchCode -&gt; Branch
     */
    private Map<String, Branch> seedBranches() {
        Map<String, Branch> branchMap = new HashMap<>();

        if (branchRepository.count() == 0) {
            Branch aiml = new Branch("AIML", "Artificial Intelligence & Machine Learning", true);
            Branch cse  = new Branch("CSE",  "Computer Science & Engineering",             true);
            Branch ece  = new Branch("ECE",  "Electronics & Communication Engineering",    true);

            List<Branch> saved = branchRepository.saveAll(List.of(aiml, cse, ece));
            for (Branch b : saved) {
                branchMap.put(b.getBranchCode(), b);
            }
            System.out.println("[DataInitializer] Seeded 3 Branches: AIML, CSE, ECE");
        } else {
            for (Branch b : branchRepository.findAll()) {
                branchMap.put(b.getBranchCode(), b);
            }
        }
        return branchMap;
    }

    // -------------------------------------------------------------------------
    // Minor Streams
    // -------------------------------------------------------------------------

    /**
     * Seeds minor streams under each branch and returns a map keyed by stream code.
     *
     * @param branches Map of branchCode -&gt; Branch
     * @return Map of streamCode -&gt; MinorStream
     */
    private Map<String, MinorStream> seedMinorStreams(Map<String, Branch> branches) {
        Map<String, MinorStream> streamMap = new HashMap<>();

        if (minorStreamRepository.count() == 0) {
            Branch aiml = branches.get("AIML");
            Branch cse  = branches.get("CSE");
            Branch ece  = branches.get("ECE");

            MinorStream mad  = new MinorStream("MAD",  "AIML MAD Stream",              aiml, true);
            MinorStream na   = new MinorStream("NA",   "AIML Network Analysis Stream", aiml, true);
            MinorStream ca   = new MinorStream("CA",   "AIML Conjugate Analysis Stream", aiml, true);
            MinorStream wd   = new MinorStream("WD",   "CSE Web Development Stream",   cse,  true);
            MinorStream cc   = new MinorStream("CC",   "CSE Cloud Computing Stream",   cse,  true);
            MinorStream vlsi = new MinorStream("VLSI", "ECE VLSI Design Stream",       ece,  true);

            List<MinorStream> saved = minorStreamRepository.saveAll(List.of(mad, na, ca, wd, cc, vlsi));
            for (MinorStream ms : saved) {
                streamMap.put(ms.getStreamCode(), ms);
            }
            System.out.println("[DataInitializer] Seeded 6 Minor Streams: MAD, NA, CA, WD, CC, VLSI");
        } else {
            for (MinorStream ms : minorStreamRepository.findAll()) {
                streamMap.put(ms.getStreamCode(), ms);
            }
        }
        return streamMap;
    }

    // -------------------------------------------------------------------------
    // Faculty
    // -------------------------------------------------------------------------

    /**
     * Seeds five faculty members and returns a map keyed by faculty code.
     *
     * @return Map of facultyCode -&gt; Faculty
     */
    private Map<String, Faculty> seedFaculty() {
        Map<String, Faculty> facultyMap = new HashMap<>();

        if (facultyRepository.count() == 0) {
            String pwd = passwordEncoder.encode("faculty123");

            Faculty f1 = new Faculty("FAC001", "Dr. Ravi Kumar",       "ravi.kumar@college.edu",     pwd, "AIML", "Professor",             "9876500001", true);
            Faculty f2 = new Faculty("FAC002", "Dr. Priya Sharma",     "priya.sharma@college.edu",   pwd, "AIML", "Associate Professor",   "9876500002", true);
            Faculty f3 = new Faculty("FAC003", "Prof. Rajesh Verma",   "rajesh.verma@college.edu",   pwd, "CSE",  "Assistant Professor",   "9876500003", true);
            Faculty f4 = new Faculty("FAC004", "Dr. Ananya Sen",       "ananya.sen@college.edu",     pwd, "ECE",  "Professor",             "9876500004", true);
            Faculty f5 = new Faculty("FAC005", "Prof. Vikram Malhotra","vikram.malhotra@college.edu", pwd, "CSE",  "Associate Professor",   "9876500005", true);

            List<Faculty> saved = facultyRepository.saveAll(List.of(f1, f2, f3, f4, f5));
            for (Faculty f : saved) {
                facultyMap.put(f.getFacultyCode(), f);
            }
            System.out.println("[DataInitializer] Seeded 5 Faculty members.");
        } else {
            for (Faculty f : facultyRepository.findAll()) {
                facultyMap.put(f.getFacultyCode(), f);
            }
        }
        return facultyMap;
    }

    // -------------------------------------------------------------------------
    // Semesters
    // -------------------------------------------------------------------------

    /**
     * Seeds semesters 3-8 for academic year 2024-25 and returns a map keyed by semester number.
     *
     * @return Map of semesterNumber (Integer) -&gt; Semester
     */
    private Map<Integer, Semester> seedSemesters() {
        Map<Integer, Semester> semesterMap = new HashMap<>();

        if (semesterRepository.count() == 0) {
            Semester s3 = new Semester(3, "2024-25", true);
            Semester s4 = new Semester(4, "2024-25", true);
            Semester s5 = new Semester(5, "2024-25", true);
            Semester s6 = new Semester(6, "2024-25", true);
            Semester s7 = new Semester(7, "2024-25", true);
            Semester s8 = new Semester(8, "2024-25", true);

            List<Semester> saved = semesterRepository.saveAll(List.of(s3, s4, s5, s6, s7, s8));
            for (Semester s : saved) {
                semesterMap.put(s.getSemesterNumber(), s);
            }
            System.out.println("[DataInitializer] Seeded 6 Semesters: 3, 4, 5, 6, 7, 8 (2024-25)");
        } else {
            for (Semester s : semesterRepository.findAll()) {
                semesterMap.put(s.getSemesterNumber(), s);
            }
        }
        return semesterMap;
    }

    // -------------------------------------------------------------------------
    // Sections
    // -------------------------------------------------------------------------

    /**
     * Seeds class sections assigned to branches, minor streams, and faculty.
     *
     * @param branches     Map of branchCode -&gt; Branch
     * @param minorStreams  Map of streamCode -&gt; MinorStream
     * @param faculty      Map of facultyCode -&gt; Faculty
     * @return Map of sectionName -&gt; Section
     */
    private Map<String, Section> seedSections(Map<String, Branch> branches,
                                               Map<String, MinorStream> minorStreams,
                                               Map<String, Faculty> faculty) {
        Map<String, Section> sectionMap = new HashMap<>();

        Branch aiml = branches.get("AIML");
        Branch cse  = branches.get("CSE");
        Branch ece  = branches.get("ECE");

        MinorStream mad  = minorStreams.get("MAD");
        MinorStream na   = minorStreams.get("NA");
        MinorStream ca   = minorStreams.get("CA");
        MinorStream wd   = minorStreams.get("WD");
        MinorStream vlsi = minorStreams.get("VLSI");

        Faculty f1 = faculty.get("FAC001");
        Faculty f2 = faculty.get("FAC002");
        Faculty f3 = faculty.get("FAC003");
        Faculty f4 = faculty.get("FAC004");

        sectionMap.put("MAD Stream-1", ensureSection("MAD Stream-1", aiml, mad, f1));
        sectionMap.put("MAD Stream-2", ensureSection("MAD Stream-2", aiml, mad, f2));
        sectionMap.put("MAD Stream-3", ensureSection("MAD Stream-3", aiml, mad, f1));
        sectionMap.put("NA Stream-1", ensureSection("NA Stream-1", aiml, na, f1));
        sectionMap.put("CA Stream-1", ensureSection("CA Stream-1", aiml, ca, f2));
        sectionMap.put("WD Stream-1", ensureSection("WD Stream-1", cse, wd, f3));
        sectionMap.put("VLSI Stream-1", ensureSection("VLSI Stream-1", ece, vlsi, f4));

        System.out.println("[DataInitializer] Ensured 7 academic sections, including MAD Stream-3.");
        return sectionMap;
    }

    private Section ensureSection(String name, Branch branch, MinorStream minorStream, Faculty faculty) {
        return sectionRepository.findAll().stream()
                .filter(s -> name.equalsIgnoreCase(s.getSectionName()))
                .findFirst()
                .orElseGet(() -> sectionRepository.save(
                        new Section(name, branch, minorStream, "2024-25", faculty, true)));
    }

    // -------------------------------------------------------------------------
    // Courses
    // -------------------------------------------------------------------------

    /**
     * Seeds 12 courses across all streams and semesters using the basic constructor
     * followed by setters for Branch, MinorStream, Semester, and Faculty associations.
     *
     * @param branches    Map of branchCode -&gt; Branch
     * @param minorStreams Map of streamCode -&gt; MinorStream
     * @param faculty     Map of facultyCode -&gt; Faculty
     * @param semesters   Map of semesterNumber -&gt; Semester
     */
    private void seedCourses(Map<String, Branch> branches,
                              Map<String, MinorStream> minorStreams,
                              Map<String, Faculty> faculty,
                              Map<Integer, Semester> semesters) {
        Branch aiml = branches.get("AIML");
        Branch cse  = branches.get("CSE");
        Branch ece  = branches.get("ECE");

        MinorStream mad  = minorStreams.get("MAD");
        MinorStream na   = minorStreams.get("NA");
        MinorStream ca   = minorStreams.get("CA");
        MinorStream wd   = minorStreams.get("WD");
        MinorStream cc   = minorStreams.get("CC");
        MinorStream vlsi = minorStreams.get("VLSI");

        Semester sem3 = semesters.get(3);
        Semester sem5 = semesters.get(5);

        Faculty f1 = faculty.get("FAC001");
        Faculty f2 = faculty.get("FAC002");
        Faculty f3 = faculty.get("FAC003");
        Faculty f4 = faculty.get("FAC004");
        Faculty f5 = faculty.get("FAC005");

        // AIML MAD Stream
        upsertCourse("MAD301", "Mobile App Development Fundamentals",
                "Android and iOS app development basics, UI design, lifecycle management.",
                "AIML", 4, "Dr. Ravi Kumar", 30, 30, "14 Weeks", "ACTIVE",
                aiml, mad, sem3, f1);

        upsertCourse("MAD501", "Advanced Mobile Development",
                "Advanced React Native, Flutter, state management, app deployment.",
                "AIML", 4, "Dr. Priya Sharma", 30, 30, "14 Weeks", "ACTIVE",
                aiml, mad, sem5, f2);

        // AIML Network Analysis Stream
        upsertCourse("NA301", "Network Analysis Fundamentals",
                "Graph theory, network topology, flow analysis, bandwidth optimization.",
                "AIML", 3, "Dr. Ravi Kumar", 25, 25, "12 Weeks", "ACTIVE",
                aiml, na, sem3, f1);

        upsertCourse("NA501", "Advanced Network Analysis",
                "Deep packet inspection, network forensics, protocol analysis.",
                "AIML", 3, "Dr. Ravi Kumar", 25, 25, "12 Weeks", "ACTIVE",
                aiml, na, sem5, f1);

        // AIML Conjugate Analysis Stream
        upsertCourse("CA301", "Conjugate Analysis Basics",
                "Mathematical foundations for conjugate systems, signal processing basics.",
                "AIML", 3, "Dr. Priya Sharma", 25, 25, "12 Weeks", "ACTIVE",
                aiml, ca, sem3, f2);

        upsertCourse("CA501", "Advanced Conjugate Systems",
                "Advanced signal decomposition, conjugate transform applications.",
                "AIML", 3, "Dr. Priya Sharma", 25, 25, "12 Weeks", "ACTIVE",
                aiml, ca, sem5, f2);

        // CSE Web Development Stream
        upsertCourse("WD301", "Web Development Fundamentals",
                "HTML5, CSS3, JavaScript ES6+, responsive design, REST APIs.",
                "CSE", 4, "Prof. Vikram Malhotra", 35, 35, "12 Weeks", "ACTIVE",
                cse, wd, sem3, f5);

        upsertCourse("WD501", "Full Stack Development",
                "React.js, Spring Boot, microservices, Docker deployment.",
                "CSE", 4, "Prof. Rajesh Verma", 35, 35, "14 Weeks", "ACTIVE",
                cse, wd, sem5, f3);

        // CSE Cloud Computing Stream
        upsertCourse("CC301", "Cloud Computing Fundamentals",
                "AWS basics, Azure fundamentals, IaaS, PaaS, SaaS, virtualization.",
                "CSE", 3, "Prof. Rajesh Verma", 30, 30, "10 Weeks", "ACTIVE",
                cse, cc, sem3, f3);

        upsertCourse("CC501", "Advanced Cloud Architecture",
                "Serverless, Kubernetes, cloud security, multi-cloud strategies.",
                "CSE", 3, "Prof. Vikram Malhotra", 30, 30, "12 Weeks", "ACTIVE",
                cse, cc, sem5, f5);

        // ECE VLSI Design Stream
        upsertCourse("VL301", "VLSI Design Fundamentals",
                "CMOS design, digital circuits, FPGA programming basics.",
                "ECE", 4, "Dr. Ananya Sen", 20, 20, "14 Weeks", "ACTIVE",
                ece, vlsi, sem3, f4);

        upsertCourse("VL501", "Advanced VLSI Architecture",
                "ASIC design, chip layout, low-power design techniques.",
                "ECE", 4, "Dr. Ananya Sen", 20, 20, "14 Weeks", "ACTIVE",
                ece, vlsi, sem5, f4);

        System.out.println("[DataInitializer] Synchronized 12 Courses with academic entities.");
    }

    private void upsertCourse(String courseCode, String courseName, String description,
                              String department, int credits, String facultyName,
                              int maxSeats, int availableSeats, String duration, String status,
                              Branch branch, MinorStream minorStream, Semester semester, Faculty faculty) {
        Course course = courseRepository.findByCourseCode(courseCode).orElse(null);
        if (course == null) {
            course = buildCourse(courseCode, courseName, description, department, credits, facultyName,
                                 maxSeats, availableSeats, duration, status, branch, minorStream, semester, faculty);
            courseRepository.save(course);
        } else {
            boolean updated = false;
            if (course.getBranch() == null) { course.setBranch(branch); updated = true; }
            if (course.getMinorStream() == null) { course.setMinorStream(minorStream); updated = true; }
            if (course.getSemester() == null) { course.setSemester(semester); updated = true; }
            if (course.getFaculty() == null) { course.setFaculty(faculty); updated = true; }
            if (course.getFacultyName() == null || course.getFacultyName().isBlank()) { course.setFacultyName(facultyName); updated = true; }
            if (course.getStatus() == null) { course.setStatus("ACTIVE"); updated = true; }
            if (updated) {
                courseRepository.save(course);
                System.out.println("[DataInitializer] Linked existing course to academic entities: " + courseCode);
            }
        }
    }

    /**
     * Helper that creates a Course using the 10-arg constructor then applies
     * Branch, MinorStream, Semester, and Faculty via setters.
     */
    private Course buildCourse(String courseCode, String courseName, String description,
                                String department, int credits, String facultyName,
                                int maxSeats, int availableSeats, String duration, String status,
                                Branch branch, MinorStream minorStream, Semester semester, Faculty faculty) {
        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setDescription(description);
        course.setDepartment(department);
        course.setCredits(credits);
        course.setFacultyName(facultyName);
        course.setMaxSeats(maxSeats);
        course.setAvailableSeats(availableSeats);
        course.setDuration(duration);
        course.setStatus(status);
        course.setBranch(branch);
        course.setMinorStream(minorStream);
        course.setSemester(semester);
        course.setFaculty(faculty);
        return course;
    }

    // -------------------------------------------------------------------------
    // Students
    // -------------------------------------------------------------------------

    /**
     * Seeds five students, each assigned to a branch, minor stream, section, and current semester.
     *
     * @param branches    Map of branchCode -&gt; Branch
     * @param minorStreams Map of streamCode -&gt; MinorStream
     * @param sections    Map of sectionName -&gt; Section
     * @param semesters   Map of semesterNumber -&gt; Semester
     */
    private void seedStudents(Map<String, Branch> branches,
                               Map<String, MinorStream> minorStreams,
                               Map<String, Section> sections,
                               Map<Integer, Semester> semesters) {
        if (studentRepository.count() != 0) {
            studentRepository.findAll().forEach(s -> {
                if (!s.getPassword().startsWith("$2a$") && !s.getPassword().startsWith("$2b$")) {
                    s.setPassword(passwordEncoder.encode(s.getPassword()));
                    studentRepository.save(s);
                    System.out.println("[DataInitializer] Upgraded password for student: " + s.getEmail());
                }
            });
            studentRepository.findByEmail("akhil@gmail.com").ifPresent(s -> {
                if (s.getBranch() == null) {
                    s.setBranch(branches.get("AIML"));
                    s.setMinorStream(minorStreams.get("MAD"));
                    s.setSection(sections.get("MAD Stream-1"));
                    s.setCurrentSemester(semesters.get(3));
                    studentRepository.save(s);
                }
            });
            studentRepository.findByEmail("sneha@gmail.com").ifPresent(s -> {
                if (s.getBranch() == null) {
                    s.setBranch(branches.get("AIML"));
                    s.setMinorStream(minorStreams.get("NA"));
                    s.setSection(sections.get("NA Stream-1"));
                    s.setCurrentSemester(semesters.get(5));
                    studentRepository.save(s);
                }
            });
            studentRepository.findByEmail("rahul@gmail.com").ifPresent(s -> {
                if (s.getBranch() == null) {
                    s.setBranch(branches.get("CSE"));
                    s.setMinorStream(minorStreams.get("WD"));
                    s.setSection(sections.get("WD Stream-1"));
                    s.setCurrentSemester(semesters.get(3));
                    studentRepository.save(s);
                }
            });
            studentRepository.findByEmail("pooja@gmail.com").ifPresent(s -> {
                if (s.getBranch() == null) {
                    s.setBranch(branches.get("AIML"));
                    s.setMinorStream(minorStreams.get("CA"));
                    s.setSection(sections.get("CA Stream-1"));
                    s.setCurrentSemester(semesters.get(5));
                    studentRepository.save(s);
                }
            });
            studentRepository.findByEmail("karan@gmail.com").ifPresent(s -> {
                if (s.getBranch() == null) {
                    s.setBranch(branches.get("ECE"));
                    s.setMinorStream(minorStreams.get("VLSI"));
                    s.setSection(sections.get("VLSI Stream-1"));
                    s.setCurrentSemester(semesters.get(3));
                    studentRepository.save(s);
                }
            });
            return;
        }

        Branch aiml = branches.get("AIML");
        Branch cse  = branches.get("CSE");
        Branch ece  = branches.get("ECE");

        MinorStream mad  = minorStreams.get("MAD");
        MinorStream na   = minorStreams.get("NA");
        MinorStream ca   = minorStreams.get("CA");
        MinorStream wd   = minorStreams.get("WD");
        MinorStream vlsi = minorStreams.get("VLSI");

        Section mad1 = sections.get("MAD Stream-1");
        Section na1  = sections.get("NA Stream-1");
        Section ca1  = sections.get("CA Stream-1");
        Section wd1  = sections.get("WD Stream-1");
        Section vl1  = sections.get("VLSI Stream-1");

        Semester sem3 = semesters.get(3);
        Semester sem5 = semesters.get(5);

        String pwd = passwordEncoder.encode("12345");

        // Akhil Sharma
        Student akhil = new Student();
        akhil.setName("Akhil Sharma");
        akhil.setEmail("akhil@gmail.com");
        akhil.setPassword(pwd);
        akhil.setRollNo("22A01");
        akhil.setDepartment("AIML");
        akhil.setYear(2);
        akhil.setPhone("9876543210");
        akhil.setBranch(aiml);
        akhil.setMinorStream(mad);
        akhil.setSection(mad1);
        akhil.setCurrentSemester(sem3);

        // Sneha Reddy
        Student sneha = new Student();
        sneha.setName("Sneha Reddy");
        sneha.setEmail("sneha@gmail.com");
        sneha.setPassword(pwd);
        sneha.setRollNo("22A14");
        sneha.setDepartment("AIML");
        sneha.setYear(3);
        sneha.setPhone("9876543211");
        sneha.setBranch(aiml);
        sneha.setMinorStream(na);
        sneha.setSection(na1);
        sneha.setCurrentSemester(sem5);

        // Rahul Verma
        Student rahul = new Student();
        rahul.setName("Rahul Verma");
        rahul.setEmail("rahul@gmail.com");
        rahul.setPassword(pwd);
        rahul.setRollNo("22C05");
        rahul.setDepartment("CSE");
        rahul.setYear(2);
        rahul.setPhone("9876543212");
        rahul.setBranch(cse);
        rahul.setMinorStream(wd);
        rahul.setSection(wd1);
        rahul.setCurrentSemester(sem3);

        // Pooja Nair
        Student pooja = new Student();
        pooja.setName("Pooja Nair");
        pooja.setEmail("pooja@gmail.com");
        pooja.setPassword(pwd);
        pooja.setRollNo("21A09");
        pooja.setDepartment("AIML");
        pooja.setYear(4);
        pooja.setPhone("9876543213");
        pooja.setBranch(aiml);
        pooja.setMinorStream(ca);
        pooja.setSection(ca1);
        pooja.setCurrentSemester(sem5);

        // Karan Joshi
        Student karan = new Student();
        karan.setName("Karan Joshi");
        karan.setEmail("karan@gmail.com");
        karan.setPassword(pwd);
        karan.setRollNo("22E32");
        karan.setDepartment("ECE");
        karan.setYear(2);
        karan.setPhone("9876543214");
        karan.setBranch(ece);
        karan.setMinorStream(vlsi);
        karan.setSection(vl1);
        karan.setCurrentSemester(sem3);

        studentRepository.saveAll(List.of(akhil, sneha, rahul, pooja, karan));
        System.out.println("[DataInitializer] Seeded 5 Students.");
    }

    // -------------------------------------------------------------------------
    // Registration Windows
    // -------------------------------------------------------------------------

    /**
     * Seeds two open registration windows (semester 3 and semester 5) if none exist.
     *
     * @param semesters Map of semesterNumber -&gt; Semester
     */
    private void seedRegistrationWindows(Map<Integer, Semester> semesters) {
        if (registrationWindowRepository.count() != 0) {
            return;
        }

        Semester sem3 = semesters.get(3);
        Semester sem5 = semesters.get(5);

        RegistrationWindow window3 = new RegistrationWindow(
                "Semester 3 Registration Window 2024-25",
                sem3,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now().plusDays(21),
                "OPEN",
                true);

        RegistrationWindow window5 = new RegistrationWindow(
                "Semester 5 Registration Window 2024-25",
                sem5,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(23),
                "OPEN",
                true);

        registrationWindowRepository.saveAll(List.of(window3, window5));
        System.out.println("[DataInitializer] Seeded 2 Registration Windows (Semester 3 & 5).");
    }
}
